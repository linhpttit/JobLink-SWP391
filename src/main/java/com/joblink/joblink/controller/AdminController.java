package com.joblink.joblink.controller;

// domain User import removed; session holds UserSessionDTO now
import com.joblink.joblink.Repository.ApplicationRepository;
import com.joblink.joblink.Repository.EmployerRepository;
import com.joblink.joblink.Repository.JobSeekerProfileRepository;
import com.joblink.joblink.Repository.UserRepository;
import com.joblink.joblink.auth.util.CurrencyUtils;
import com.joblink.joblink.entity.Blog;
import com.joblink.joblink.entity.BlogPost;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.entity.JobSeekerProfile;
import com.joblink.joblink.entity.User;
import com.joblink.joblink.service.BlogPostService;
import com.joblink.joblink.service.DashboardService;
import com.joblink.joblink.service.JobSeekerService;
import com.joblink.joblink.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JobSeekerService jobSeekerService;
    @Autowired
    private JobSeekerProfileRepository jobSeekerRepo;
    @Autowired
    private EmployerRepository employerRepository;
    @Autowired
    private com.joblink.joblink.service.BlogService blogService;
    @Autowired
    private BlogPostService blogPostService;

    private boolean ensureAdmin(HttpSession session) {
        com.joblink.joblink.dto.UserSessionDTO u = (com.joblink.joblink.dto.UserSessionDTO) session.getAttribute("user");
        return u != null && "admin".equalsIgnoreCase(u.getRole());
    }

    private void putUser(Model model, HttpSession session) {
        model.addAttribute("user", session.getAttribute("user"));
    }

    @GetMapping({"", "/"})
    public String adminShell(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "admin";
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, @RequestParam(name = "days", required = false, defaultValue = "30") int days) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);

        // Sử dụng một service thống nhất
        long totalUsers = userService.getTotalUsers();
        long jobSeekers = dashboardService.countJobSeeker();
        long employers = dashboardService.countEmployers();
        long jobPosts = dashboardService.countJobPosts();
        long applications = dashboardService.countApplications();
        double revenue = dashboardService.getTotalRevenue();
        String formattedRevenue = CurrencyUtils.formatVND(revenue);

        // Thống kê cho biểu đồ
        long totalApplications = applicationRepository.count();
        long acceptedApplications = applicationRepository.countByStatus("accepted");

        // Đảm bảo không trùng lặp
        long jobSeekersCount = userRepository.countByRole("seeker");
        long employersCount = userRepository.countByRole("employer");
        long adminsCount = userRepository.countByRole("admin");

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("jobSeekers", jobSeekersCount); // Sử dụng biến không trùng
        model.addAttribute("employers", employersCount);   // Sử dụng biến không trùng
        model.addAttribute("jobPosts", jobPosts);
        model.addAttribute("applications", applications);
        model.addAttribute("revenue", formattedRevenue);
        model.addAttribute("admins", adminsCount);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("acceptedApplications", acceptedApplications);

        return "dashboard";
    }

    @GetMapping("/jobseeker")
    public String jobseeker(Model model, HttpSession session,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) Integer experience) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);

        // Gọi service
        List<JobSeekerProfile> jobSeekers = jobSeekerService.search(search, experience, status);

        // Thống kê
        long total = jobSeekerService.countJobSeeker();
        long active = jobSeekerService.countActive();
        long locked = jobSeekerService.countLocked();
        long cv = jobSeekerService.countCV();

        // Truyền vào model
        model.addAttribute("jobSeekers", jobSeekers);
        model.addAttribute("totalJobSeekers", total);
        model.addAttribute("activeJobSeekers", active);
        model.addAttribute("lockedJobSeekers", locked);
        model.addAttribute("totalCVs", cv);

        // Giữ lại giá trị filter khi người dùng submit
        model.addAttribute("searchValue", search);
        model.addAttribute("statusValue", status);
        model.addAttribute("experienceValue", experience);

        return "jobseeker";
    }

    @GetMapping("/jobseeker/search")
    @ResponseBody
    public Map<String, Object> searchJobSeekers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            HttpSession session) {

        if (!ensureAdmin(session)) {
            System.out.println("❌ Không phải admin hoặc chưa đăng nhập");
            return Map.of("data", List.of(), "total", 0L, "page", 1, "totalPages", 0);
        }

        // Xử lý experience từ string sang integer
        Integer expValue = null;
        if (experience != null && !experience.trim().isEmpty()) {
            try {
                expValue = Integer.parseInt(experience);
            } catch (NumberFormatException e) {
                expValue = null;
            }
        }

        // Đảm bảo page và size hợp lệ
        if (page < 1) page = 1;
        if (size < 1) size = 5;

        // Log để debug
        System.out.println("🔍 Search params - keyword: " + keyword + ", experience: " + expValue + ", status: " + status + ", page: " + page + ", size: " + size);

        // Lấy tổng số kết quả
        long total = jobSeekerService.countSearch(keyword, expValue, status);
        int totalPages = (int) Math.ceil((double) total / size);

        // Lấy dữ liệu với pagination
        List<JobSeekerProfile> results = jobSeekerService.searchPaginated(keyword, expValue, status, page, size);

        System.out.println("✅ Found " + results.size() + " results, total: " + total + ", page: " + page + "/" + totalPages);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", results);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", totalPages);
        return response;
    }

    @GetMapping("/api/jobseekers/stats")
    @ResponseBody
    public Map<String, Long> getJobSeekerStats(HttpSession session) {
        if (!ensureAdmin(session)) {
            return Map.of("total", 0L, "active", 0L, "locked", 0L, "cv", 0L);
        }

        try {
            long total = jobSeekerService.countJobSeeker();
            long active = jobSeekerService.countActive();
            long locked = jobSeekerService.countLocked();
            long cv = jobSeekerService.countCV();

            System.out.println("📊 Stats - total: " + total + ", active: " + active + ", locked: " + locked + ", cv: " + cv);
            return Map.of("total", total, "active", active, "locked", locked, "cv", cv);
        } catch (Exception e) {
            System.err.println("❌ Error getting stats: " + e.getMessage());
            e.printStackTrace();
            return Map.of("total", 0L, "active", 0L, "locked", 0L, "cv", 0L);
        }
    }

    @DeleteMapping("/jobseeker/{seekerId}")
    @ResponseBody
    public Map<String, Object> softDeleteJobSeeker(@PathVariable Integer seekerId, HttpSession session) {
        if (!ensureAdmin(session)) {
            return Map.of("success", false, "message", "Không có quyền truy cập");
        }

        try {
            System.out.println("🗑️ Đang xóa mềm job seeker với ID: " + seekerId);
            boolean success = jobSeekerService.softDelete(seekerId);
            
            if (success) {
                System.out.println("✅ Đã khóa job seeker ID: " + seekerId);
                return Map.of("success", true, "message", "Đã khóa tài khoản thành công");
            } else {
                System.out.println("❌ Không tìm thấy job seeker ID: " + seekerId);
                return Map.of("success", false, "message", "Không tìm thấy người tìm việc");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa mềm: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }


    @GetMapping("/recruitment")
    public String recruitment(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "recruitment"; // template file is recruitment.html in templates
    }

    @GetMapping("/employer")
    public String employer(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFilter,
            Model model, 
            HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        
        // Lấy danh sách employers với filter
        List<Employer> employers;
        if ((keyword != null && !keyword.trim().isEmpty()) ||
            (status != null && !status.trim().isEmpty()) ||
            (dateFilter != null && !dateFilter.trim().isEmpty())) {
            // Có filter -> search
            String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            String searchStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
            String searchDate = (dateFilter != null && !dateFilter.trim().isEmpty()) ? dateFilter.trim() : null;
            
            // Tìm IDs trước (bỏ industry parameter)
            List<Long> employerIds = employerRepository.searchEmployerIds(searchKeyword, searchStatus, null, searchDate);
            
            // Load đầy đủ với user nếu có kết quả
            if (employerIds.isEmpty()) {
                employers = List.of();
            } else {
                employers = employerRepository.findByIdsWithUser(employerIds);
            }
        } else {
            // Không có filter -> lấy tất cả
            employers = employerRepository.findAllWithUser();
        }
        
        // Tạo DTO list với status đã tính toán sẵn
        List<Map<String, Object>> employerList = employers.stream()
            .map(emp -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("employerId", emp.getId());
                map.put("userId", emp.getUser() != null ? emp.getUser().getUserId() : null);
                map.put("companyName", emp.getCompanyName());
                map.put("email", emp.getUser() != null ? emp.getUser().getEmail() : "");
                map.put("phoneNumber", emp.getPhoneNumber());
                map.put("industry", emp.getIndustry());
                map.put("location", emp.getLocation());
                map.put("createdAt", emp.getUser() != null ? emp.getUser().getCreatedAt() : null);
                map.put("enabled", emp.getUser() != null ? emp.getUser().getEnabled() : null);
                
                // Xác định trạng thái
                boolean isEnabled = emp.getUser() != null && emp.getUser().getEnabled() != null && emp.getUser().getEnabled();
                boolean isPending = false;
                if (isEnabled && emp.getUser().getCreatedAt() != null) {
                    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
                    isPending = emp.getUser().getCreatedAt().isAfter(sevenDaysAgo);
                }
                
                if (isPending) {
                    map.put("status", "pending");
                    map.put("statusText", "Chờ xét duyệt");
                } else if (isEnabled) {
                    map.put("status", "active");
                    map.put("statusText", "Đang hoạt động");
                } else {
                    map.put("status", "inactive");
                    map.put("statusText", "Ngừng hoạt động");
                }
                
                return map;
            })
            .toList();
        
        // Thống kê
        long total = userRepository.countEmployers();
        long active = userRepository.countActiveEmployers();
        long pending = userRepository.countPendingEmployers();
        long inactive = userRepository.countInactiveEmployers();
        
        // Truyền vào model
        model.addAttribute("employers", employerList);
        model.addAttribute("totalEmployers", total);
        model.addAttribute("activeEmployers", active);
        model.addAttribute("pendingEmployers", pending);
        model.addAttribute("inactiveEmployers", inactive);
        
        // Truyền filter values để giữ lại trong form
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("status", status != null ? status : "");
        model.addAttribute("dateFilter", dateFilter != null ? dateFilter : "");
        
        return "employer";
    }

    // API endpoint để chỉ lấy dữ liệu bảng employer (không load full page)
    @GetMapping("/api/employers/table")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEmployerTableData(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            HttpSession session) {
        
        if (!ensureAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Không có quyền truy cập"));
        }
        
        // Đảm bảo page và size hợp lệ
        if (page < 1) page = 1;
        if (size < 1) size = 5;
        
        // Tính offset
        int offset = (page - 1) * size;
        
        List<Employer> employers;
        long total;
        int totalPages;
        
        if ((keyword != null && !keyword.trim().isEmpty()) ||
            (status != null && !status.trim().isEmpty()) ||
            (dateFilter != null && !dateFilter.trim().isEmpty())) {
            
            String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            String searchStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
            String searchDate = (dateFilter != null && !dateFilter.trim().isEmpty()) ? dateFilter.trim() : null;
            
            // Đếm tổng số kết quả
            total = employerRepository.countSearchEmployerIds(searchKeyword, searchStatus, null, searchDate);
            totalPages = (int) Math.ceil((double) total / size);
            
            // Lấy dữ liệu với pagination
            List<Long> employerIds = employerRepository.searchEmployerIdsPaginated(searchKeyword, searchStatus, null, searchDate, offset, size);
            
            if (employerIds.isEmpty()) {
                employers = List.of();
            } else {
                employers = employerRepository.findByIdsWithUser(employerIds);
            }
        } else {
            // Không có filter -> lấy tất cả với pagination
            total = employerRepository.countAllEmployerIds();
            totalPages = (int) Math.ceil((double) total / size);
            
            List<Long> employerIds = employerRepository.findAllEmployerIdsPaginated(offset, size);
            
            if (employerIds.isEmpty()) {
                employers = List.of();
            } else {
                employers = employerRepository.findByIdsWithUser(employerIds);
            }
        }
        
        List<Map<String, Object>> employerList = employers.stream()
            .map(emp -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("employerId", emp.getId());
                map.put("userId", emp.getUser() != null ? emp.getUser().getUserId() : null);
                map.put("companyName", emp.getCompanyName());
                map.put("email", emp.getUser() != null ? emp.getUser().getEmail() : "");
                map.put("phoneNumber", emp.getPhoneNumber());
                map.put("industry", emp.getIndustry());
                map.put("location", emp.getLocation());
                map.put("createdAt", emp.getUser() != null ? emp.getUser().getCreatedAt() : null);
                map.put("enabled", emp.getUser() != null ? emp.getUser().getEnabled() : null);
                
                boolean isEnabled = emp.getUser() != null && emp.getUser().getEnabled() != null && emp.getUser().getEnabled();
                boolean isPending = false;
                if (isEnabled && emp.getUser().getCreatedAt() != null) {
                    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
                    isPending = emp.getUser().getCreatedAt().isAfter(sevenDaysAgo);
                }
                
                if (isPending) {
                    map.put("status", "pending");
                    map.put("statusText", "Chờ xét duyệt");
                } else if (isEnabled) {
                    map.put("status", "active");
                    map.put("statusText", "Đang hoạt động");
                } else {
                    map.put("status", "inactive");
                    map.put("statusText", "Ngừng hoạt động");
                }
                
                return map;
            })
            .toList();
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("employers", employerList);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", totalPages);
        
        return ResponseEntity.ok(response);
    }

    // API endpoint để xuất Excel cho employer
    @GetMapping("/employer/export")
    public void exportEmployersToExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFilter,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        
        if (!ensureAdmin(session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền truy cập");
            return;
        }

        // Lấy dữ liệu employer với filter (sử dụng logic tương tự như API table)
        List<Employer> employers;
        if ((keyword != null && !keyword.trim().isEmpty()) ||
            (status != null && !status.trim().isEmpty()) ||
            (dateFilter != null && !dateFilter.trim().isEmpty())) {
            
            String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            String searchStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
            String searchDate = (dateFilter != null && !dateFilter.trim().isEmpty()) ? dateFilter.trim() : null;
            
            List<Long> employerIds = employerRepository.searchEmployerIds(searchKeyword, searchStatus, null, searchDate);
            
            if (employerIds.isEmpty()) {
                employers = List.of();
            } else {
                employers = employerRepository.findByIdsWithUser(employerIds);
            }
        } else {
            employers = employerRepository.findAllWithUser();
        }

        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách nhà tuyển dụng");

        // Tạo header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID", "Tên công ty", "Email", "Số điện thoại", "Ngành nghề", 
            "Địa chỉ", "Ngày đăng ký", "Trạng thái"
        };
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Tạo data rows
        int rowNum = 1;
        for (Employer emp : employers) {
            Row row = sheet.createRow(rowNum++);
            
            // Xác định trạng thái
            boolean isEnabled = emp.getUser() != null && emp.getUser().getEnabled() != null && emp.getUser().getEnabled();
            boolean isPending = false;
            String statusText = "Ngừng hoạt động";
            
            if (isEnabled && emp.getUser().getCreatedAt() != null) {
                LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
                isPending = emp.getUser().getCreatedAt().isAfter(sevenDaysAgo);
            }
            
            if (isPending) {
                statusText = "Chờ xét duyệt";
            } else if (isEnabled) {
                statusText = "Đang hoạt động";
            }
            
            // Điền dữ liệu
            row.createCell(0).setCellValue(emp.getId() != null ? emp.getId().toString() : "");
            row.createCell(1).setCellValue(emp.getCompanyName() != null ? emp.getCompanyName() : "");
            row.createCell(2).setCellValue(emp.getUser() != null && emp.getUser().getEmail() != null ? emp.getUser().getEmail() : "");
            row.createCell(3).setCellValue(emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "");
            row.createCell(4).setCellValue(emp.getIndustry() != null ? emp.getIndustry() : "");
            row.createCell(5).setCellValue(emp.getLocation() != null ? emp.getLocation() : "");
            row.createCell(6).setCellValue(emp.getUser() != null && emp.getUser().getCreatedAt() != null ? 
                emp.getUser().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
            row.createCell(7).setCellValue(statusText);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Set response headers
        String fileName = "danh_sach_nha_tuyen_dung_" + 
            java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Write workbook to response
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/employer/{userId}/soft-delete")
    public String softDeleteEmployer(@PathVariable Integer userId, HttpSession session) {
        if (!ensureAdmin(session)) {
            return "redirect:/signin";
        }

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                System.err.println("❌ Không tìm thấy user với ID: " + userId);
                return "redirect:/admin/employer?error=notfound";
            }

            // Kiểm tra role phải là employer
            if (!"employer".equalsIgnoreCase(user.getRole())) {
                System.err.println("❌ User không phải employer: " + userId);
                return "redirect:/admin/employer?error=invalid";
            }

            // Soft delete: set enabled = false
            user.setEnabled(false);
            userRepository.save(user);

            System.out.println("✅ Đã khóa employer với user_id: " + userId);
            return "redirect:/admin/employer?success=deleted";
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa mềm employer: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/employer?error=server";
        }
    }

    @GetMapping("/api/employers/stats")
    @ResponseBody
    public Map<String, Long> getEmployerStats(HttpSession session) {
        if (!ensureAdmin(session)) {
            return Map.of("total", 0L, "active", 0L, "pending", 0L, "inactive", 0L);
        }

        try {
            long total = userRepository.countEmployers();
            long active = userRepository.countActiveEmployers();
            long pending = userRepository.countPendingEmployers();
            long inactive = userRepository.countInactiveEmployers();

            System.out.println("📊 Employer Stats - total: " + total + ", active: " + active + ", pending: " + pending + ", inactive: " + inactive);
            return Map.of("total", total, "active", active, "pending", pending, "inactive", inactive);
        } catch (Exception e) {
            System.err.println("❌ Error getting employer stats: " + e.getMessage());
            e.printStackTrace();
            return Map.of("total", 0L, "active", 0L, "pending", 0L, "inactive", 0L);
        }
    }

    @GetMapping("/applications")
    public String applications(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "applications";
    }

    @GetMapping("/companies")
    public String companies(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "companies";
    }

    @GetMapping("/blog")
    public String blog(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        model.addAttribute("blogs", blogService.getAllBlogs());
        model.addAttribute("posts", blogPostService.getAllPost());
        return "blog";
    }

    @PostMapping("/blog/delete/{id}")
    public String deleteBlog(@PathVariable int id, Model model, HttpSession session) {
        blogService.softDeleteBlog(id);
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        model.addAttribute("blogs", blogService.getAllBlogs());
        model.addAttribute("posts", blogPostService.getAllPost());
        return "blog";
    }

    @PostMapping("/blogpost/delete/{id}")
    public String deleteBlogPost(@PathVariable int id, Model model, HttpSession session) {
        blogPostService.softDeleteBlog(id);
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        model.addAttribute("blogs", blogService.getAllBlogs());
        model.addAttribute("posts", blogPostService.getAllPost());
        return "blog";
    }

    @GetMapping("/blog/{id}")
    @ResponseBody
    public ResponseEntity<Blog> getBlog(@PathVariable int id) {
        return blogService.getBlogById(id)
                .map(blog -> ResponseEntity.ok(blog))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/blogpost/{id}")
    @ResponseBody
    public ResponseEntity<BlogPost> getBlogPost(@PathVariable int id) {
        return blogPostService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/blog/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createBlog(@RequestParam String title,
                                                          @RequestParam String content,
                                                          @RequestParam int categoryId,
                                                          @RequestParam(required = false) Integer parentBlogId) {
        try {
            Blog newBlog = new Blog();
            newBlog.setTitle(title);
            newBlog.setContent(content);
            newBlog.setCategoryId(categoryId);
            newBlog.setParentBlogId(parentBlogId);
            
            Blog saved = blogService.createBlog(newBlog);
            return ResponseEntity.ok(Map.of("success", true, "blog", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/blog/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBlog(@PathVariable int id, 
                                                          @RequestParam String title,
                                                          @RequestParam String content,
                                                          @RequestParam int categoryId,
                                                          @RequestParam(required = false) Integer parentBlogId) {
        try {
            Blog updatedBlog = new Blog();
            updatedBlog.setTitle(title);
            updatedBlog.setContent(content);
            updatedBlog.setCategoryId(categoryId);
            updatedBlog.setParentBlogId(parentBlogId);
            
            Blog saved = blogService.updateBlog(id, updatedBlog);
            return ResponseEntity.ok(Map.of("success", true, "blog", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/blogpost/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createBlogPost(@RequestParam String title,
                                                              @RequestParam String content,
                                                              @RequestParam int categoryId) {
        try {
            BlogPost post = new BlogPost();
            post.setTitle(title);
            post.setContent(content);
            post.setCategoryId(categoryId);
            BlogPost saved = blogPostService.createPost(post);
            return ResponseEntity.ok(Map.of("success", true, "post", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/blogpost/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBlogPost(@PathVariable int id,
                                                              @RequestParam String title,
                                                              @RequestParam String content,
                                                              @RequestParam int categoryId) {
        try {
            BlogPost updated = new BlogPost();
            updated.setTitle(title);
            updated.setContent(content);
            updated.setCategoryId(categoryId);
            BlogPost saved = blogPostService.updatePost(id, updated);
            return ResponseEntity.ok(Map.of("success", true, "post", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }


    @GetMapping("/premium")
    public String premium(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "premium";
    }

    @GetMapping("/payments")
    public String payments(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "payment";
    }

    @GetMapping("/feedbacks")
    public String feedbacks(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "feedbacks";
    }

    @GetMapping("/statistic")
    public String statistic(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "statistic";
    }

    @GetMapping("/jobseeker/export")
    public void exportJobSeekersToExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String status,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        
        if (!ensureAdmin(session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền truy cập");
            return;
        }

        // Xử lý experience từ string sang integer
        Integer expValue = null;
        if (experience != null && !experience.trim().isEmpty()) {
            try {
                expValue = Integer.parseInt(experience);
            } catch (NumberFormatException e) {
                expValue = null;
            }
        }

        // Lấy danh sách job seekers (có thể có filter)
        List<JobSeekerProfile> jobSeekers = jobSeekerService.search(keyword, expValue, status);

        // Tạo workbook Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách Job Seekers");

        // Tạo style cho header
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Tạo header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Họ và tên", "Email", "Số điện thoại", "Giới tính", 
                           "Địa chỉ", "Kinh nghiệm (năm)", "Vị trí", "Trạng thái", "Ngày cập nhật"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Tạo style cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Điền dữ liệu
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int rowNum = 1;
        for (JobSeekerProfile j : jobSeekers) {
            Row row = sheet.createRow(rowNum++);
            
            // ID
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(j.getSeekerId() != null ? j.getSeekerId() : 0);
            cell0.setCellStyle(dataStyle);
            
            // Họ và tên
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(j.getFullName() != null ? j.getFullName() : "");
            cell1.setCellStyle(dataStyle);
            
            // Email
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(j.getEmail() != null ? j.getEmail() : "");
            cell2.setCellStyle(dataStyle);
            
            // Số điện thoại
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(j.getPhone() != null ? j.getPhone() : "");
            cell3.setCellStyle(dataStyle);
            
            // Giới tính
            Cell cell4 = row.createCell(4);
            String gender = j.getGender();
            if (gender != null) {
                cell4.setCellValue(gender.equals("male") ? "Nam" : gender.equals("female") ? "Nữ" : gender);
            } else {
                cell4.setCellValue("");
            }
            cell4.setCellStyle(dataStyle);
            
            // Địa chỉ
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(j.getAddress() != null ? j.getAddress() : "");
            cell5.setCellStyle(dataStyle);
            
            // Kinh nghiệm
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(j.getExperienceYears() != null ? j.getExperienceYears() : 0);
            cell6.setCellStyle(dataStyle);
            
            // Vị trí
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(j.getHeadline() != null ? j.getHeadline() : "");
            cell7.setCellStyle(dataStyle);
            
            // Trạng thái
            Cell cell8 = row.createCell(8);
            boolean isActive = j.getIsLocked() == false && (j.getReceiveInvitations() != null && j.getReceiveInvitations() == true);
            cell8.setCellValue(isActive ? "Đang hoạt động" : "Đã khóa");
            cell8.setCellStyle(dataStyle);
            
            // Ngày cập nhật
            Cell cell9 = row.createCell(9);
            if (j.getUpdatedAt() != null) {
                cell9.setCellValue(j.getUpdatedAt().format(dateFormatter));
            } else {
                cell9.setCellValue("");
            }
            cell9.setCellStyle(dataStyle);
        }

        // Tự động điều chỉnh độ rộng cột
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // Tăng thêm một chút để text không bị cắt
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        // Thiết lập response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "Danh_sach_Job_Seekers_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Ghi workbook vào response
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
