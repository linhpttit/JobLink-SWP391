package com.joblink.joblink.controller;

// domain User import removed; session holds UserSessionDTO now
import com.joblink.joblink.Repository.ApplicationRepository;
import com.joblink.joblink.Repository.EmployerRepository;
import com.joblink.joblink.Repository.InvoiceRepository;
import com.joblink.joblink.Repository.JobPostingRepository;
import com.joblink.joblink.Repository.JobSeekerProfileRepository;
import com.joblink.joblink.Repository.PaymentRepository;
import com.joblink.joblink.Repository.UserRepository;
import com.joblink.joblink.auth.util.CurrencyUtils;
import com.joblink.joblink.entity.Blog;
import com.joblink.joblink.entity.BlogPost;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.entity.Invoice;
import com.joblink.joblink.entity.JobSeekerProfile;
import com.joblink.joblink.entity.Payment;
import com.joblink.joblink.entity.User;
import com.joblink.joblink.service.ApplicationService;
import com.joblink.joblink.service.BlogPostService;
import com.joblink.joblink.service.DashboardService;
import com.joblink.joblink.service.JobSeekerService;
import com.joblink.joblink.service.UserService;
import com.joblink.joblink.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

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
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private com.joblink.joblink.Repository.PremiumPackageRepository premiumPackageRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private com.joblink.joblink.Repository.CompanyReviewRepository companyReviewRepository;
    @Autowired
    private JobPostingRepository jobPostingRepository;

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
        long totalCVs = jobSeekerService.countCV();
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
        model.addAttribute("totalCVs", totalCVs);
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

        // Truyền vào model
        model.addAttribute("jobSeekers", jobSeekers);
        model.addAttribute("totalJobSeekers", total);
        model.addAttribute("activeJobSeekers", active);
        model.addAttribute("lockedJobSeekers", locked);

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
            return Map.of("total", 0L, "active", 0L, "locked", 0L);
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
    public String applications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer jobId,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpSession session) {
        try {
            if (!ensureAdmin(session)) return "redirect:/signin";
            putUser(model, session);

            // Thống kê CV và Applications
            long totalCVs = jobSeekerService.countCV();
            long reviewingCVs = dashboardService.countReviewingCVs();
            long acceptedCVs = applicationRepository.countByStatus("accepted");
            long deniedCVs = applicationRepository.countByStatus("denied");

            model.addAttribute("totalCVs", totalCVs);
            model.addAttribute("reviewingCVs", reviewingCVs);
            model.addAttribute("acceptedCVs", acceptedCVs);
            model.addAttribute("deniedCVs", deniedCVs);

            // Xử lý filter và pagination
            if (page < 0) page = 0;
            if (size < 1) size = 5;

            // Lấy TẤT CẢ applications (không paginate) để filter
            Pageable allPageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Application> allApplicationsPage = applicationService.getApplicationsSimple(allPageable);
            List<Application> allApplications = new java.util.ArrayList<>(allApplicationsPage.getContent());

            // Lọc theo search
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                allApplications = allApplications.stream()
                        .filter(app ->
                                (app.getCandidateName() != null && app.getCandidateName().toLowerCase().contains(searchLower)) ||
                                        (app.getCandidateEmail() != null && app.getCandidateEmail().toLowerCase().contains(searchLower)) ||
                                        (app.getPosition() != null && app.getPosition().toLowerCase().contains(searchLower))
                        )
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo status
            if (status != null && !status.trim().isEmpty()) {
                allApplications = allApplications.stream()
                        .filter(app -> status.equalsIgnoreCase(app.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo jobId
            if (jobId != null) {
                allApplications = allApplications.stream()
                        .filter(app -> app.getJobId() != null && app.getJobId().equals(jobId))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo dateFilter
            if (dateFilter != null && !dateFilter.trim().isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime filterDate = null;

                switch (dateFilter) {
                    case "today":
                        filterDate = now.minusDays(1);
                        break;
                    case "week":
                        filterDate = now.minusWeeks(1);
                        break;
                    case "month":
                        filterDate = now.minusMonths(1);
                        break;
                }

                if (filterDate != null) {
                    final LocalDateTime finalFilterDate = filterDate;
                    allApplications = allApplications.stream()
                            .filter(app -> app.getAppliedAt() != null && app.getAppliedAt().isAfter(finalFilterDate))
                            .collect(java.util.stream.Collectors.toList());
                }
            }

            // Tính total và totalPages sau khi filter
            long total = allApplications.size();
            int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;

            // Paginate sau khi filter
            int start = page * size;
            int end = Math.min(start + size, allApplications.size());
            List<Application> applications = start < allApplications.size()
                    ? allApplications.subList(start, end)
                    : new java.util.ArrayList<>();

            // Add to model - đảm bảo tất cả biến đều có giá trị
            model.addAttribute("applications", applications != null ? applications : new java.util.ArrayList<>());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("total", total);
            model.addAttribute("size", size);
            model.addAttribute("search", search != null ? search : "");
            model.addAttribute("status", status != null ? status : "");
            model.addAttribute("jobId", jobId);
            model.addAttribute("dateFilter", dateFilter != null ? dateFilter : "");

            return "applications";
        } catch (Exception e) {
            System.err.println("❌ Error in applications endpoint: " + e.getMessage());
            e.printStackTrace();
            // Trả về trang với dữ liệu rỗng nếu có lỗi, nhưng vẫn giữ thống kê
            try {
                long totalCVs = jobSeekerService.countCV();
                long reviewingCVs = dashboardService.countReviewingCVs();
                long acceptedCVs = applicationRepository.countByStatus("accepted");
                long deniedCVs = applicationRepository.countByStatus("denied");

                model.addAttribute("totalCVs", totalCVs);
                model.addAttribute("reviewingCVs", reviewingCVs);
                model.addAttribute("acceptedCVs", acceptedCVs);
                model.addAttribute("deniedCVs", deniedCVs);
            } catch (Exception ex) {
                System.err.println("❌ Error getting stats: " + ex.getMessage());
                model.addAttribute("totalCVs", 0L);
                model.addAttribute("reviewingCVs", 0L);
                model.addAttribute("acceptedCVs", 0L);
                model.addAttribute("deniedCVs", 0L);
            }

            model.addAttribute("applications", new java.util.ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("total", 0L);
            model.addAttribute("size", 5);
            model.addAttribute("search", search != null ? search : "");
            model.addAttribute("status", status != null ? status : "");
            model.addAttribute("jobId", jobId);
            model.addAttribute("dateFilter", dateFilter != null ? dateFilter : "");
            return "applications";
        }
    }

    // Endpoint trả về HTML fragment (chỉ table và pagination) cho AJAX
    @GetMapping("/applications/table")
    public String applicationsTable(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer jobId,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpSession session) {
        try {
            if (!ensureAdmin(session)) {
                return "fragments/error :: error";
            }

            // Xử lý filter và pagination (giống như endpoint chính)
            if (page < 0) page = 0;
            if (size < 1) size = 5;

            // Lấy TẤT CẢ applications (không paginate) để filter
            Pageable allPageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Application> allApplicationsPage = applicationService.getApplicationsSimple(allPageable);
            List<Application> allApplications = new java.util.ArrayList<>(allApplicationsPage.getContent());

            // Lọc theo search
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                allApplications = allApplications.stream()
                        .filter(app ->
                                (app.getCandidateName() != null && app.getCandidateName().toLowerCase().contains(searchLower)) ||
                                        (app.getCandidateEmail() != null && app.getCandidateEmail().toLowerCase().contains(searchLower)) ||
                                        (app.getPosition() != null && app.getPosition().toLowerCase().contains(searchLower))
                        )
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo status
            if (status != null && !status.trim().isEmpty()) {
                allApplications = allApplications.stream()
                        .filter(app -> status.equalsIgnoreCase(app.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo jobId
            if (jobId != null) {
                allApplications = allApplications.stream()
                        .filter(app -> app.getJobId() != null && app.getJobId().equals(jobId))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo dateFilter
            if (dateFilter != null && !dateFilter.trim().isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime filterDate = null;

                switch (dateFilter) {
                    case "today":
                        filterDate = now.minusDays(1);
                        break;
                    case "week":
                        filterDate = now.minusWeeks(1);
                        break;
                    case "month":
                        filterDate = now.minusMonths(1);
                        break;
                }

                if (filterDate != null) {
                    final LocalDateTime finalFilterDate = filterDate;
                    allApplications = allApplications.stream()
                            .filter(app -> app.getAppliedAt() != null && app.getAppliedAt().isAfter(finalFilterDate))
                            .collect(java.util.stream.Collectors.toList());
                }
            }

            // Tính total và totalPages sau khi filter
            long total = allApplications.size();
            int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;

            // Paginate sau khi filter
            int start = page * size;
            int end = Math.min(start + size, allApplications.size());
            List<Application> applications = start < allApplications.size()
                    ? allApplications.subList(start, end)
                    : new java.util.ArrayList<>();

            // Add to model
            model.addAttribute("applications", applications != null ? applications : new java.util.ArrayList<>());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("total", total);
            model.addAttribute("size", size);
            model.addAttribute("search", search != null ? search : "");
            model.addAttribute("status", status != null ? status : "");
            model.addAttribute("jobId", jobId);
            model.addAttribute("dateFilter", dateFilter != null ? dateFilter : "");

            return "fragments/applications-table :: table";
        } catch (Exception e) {
            System.err.println("❌ Error in applicationsTable endpoint: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("applications", new java.util.ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("total", 0L);
            model.addAttribute("size", 5);
            return "fragments/applications-table :: table";
        }
    }

    // Endpoint để xuất Excel cho applications
    @GetMapping("/applications/export")
    public void exportApplicationsToExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer jobId,
            @RequestParam(required = false) String dateFilter,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        if (!ensureAdmin(session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền truy cập");
            return;
        }

        try {
            // Lấy TẤT CẢ applications (không paginate) để filter - giống logic trong applications endpoint
            Pageable allPageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Application> allApplicationsPage = applicationService.getApplicationsSimple(allPageable);
            List<Application> allApplications = new java.util.ArrayList<>(allApplicationsPage.getContent());

            // Lọc theo search
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                allApplications = allApplications.stream()
                        .filter(app ->
                                (app.getCandidateName() != null && app.getCandidateName().toLowerCase().contains(searchLower)) ||
                                        (app.getCandidateEmail() != null && app.getCandidateEmail().toLowerCase().contains(searchLower)) ||
                                        (app.getPosition() != null && app.getPosition().toLowerCase().contains(searchLower))
                        )
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo status
            if (status != null && !status.trim().isEmpty()) {
                allApplications = allApplications.stream()
                        .filter(app -> status.equalsIgnoreCase(app.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo jobId
            if (jobId != null) {
                allApplications = allApplications.stream()
                        .filter(app -> app.getJobId() != null && app.getJobId().equals(jobId))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo dateFilter
            if (dateFilter != null && !dateFilter.trim().isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime filterDate = null;

                switch (dateFilter) {
                    case "today":
                        filterDate = now.minusDays(1);
                        break;
                    case "week":
                        filterDate = now.minusWeeks(1);
                        break;
                    case "month":
                        filterDate = now.minusMonths(1);
                        break;
                }

                if (filterDate != null) {
                    final LocalDateTime finalFilterDate = filterDate;
                    allApplications = allApplications.stream()
                            .filter(app -> app.getAppliedAt() != null && app.getAppliedAt().isAfter(finalFilterDate))
                            .collect(java.util.stream.Collectors.toList());
                }
            }

            // Tạo workbook và sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Danh sách ứng viên");

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
            String[] headers = {
                    "ID", "Tên ứng viên", "Email", "Số điện thoại", "Công việc",
                    "Ngày nộp", "Trạng thái", "CV/Tài liệu", "Ghi chú"
            };

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
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int rowNum = 1;
            for (Application app : allApplications) {
                Row row = sheet.createRow(rowNum++);

                // ID
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(app.getApplicationId() != null ? app.getApplicationId() : 0);
                cell0.setCellStyle(dataStyle);

                // Tên ứng viên
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(app.getCandidateName() != null ? app.getCandidateName() : "");
                cell1.setCellStyle(dataStyle);

                // Email
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(app.getCandidateEmail() != null ? app.getCandidateEmail() : "");
                cell2.setCellStyle(dataStyle);

                // Số điện thoại
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(app.getCandidatePhone() != null ? app.getCandidatePhone() : "");
                cell3.setCellStyle(dataStyle);

                // Công việc
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(app.getPosition() != null ? app.getPosition() : "");
                cell4.setCellStyle(dataStyle);

                // Ngày nộp
                Cell cell5 = row.createCell(5);
                if (app.getAppliedAt() != null) {
                    cell5.setCellValue(app.getAppliedAt().format(dateFormatter));
                } else {
                    cell5.setCellValue("");
                }
                cell5.setCellStyle(dataStyle);

                // Trạng thái
                Cell cell6 = row.createCell(6);
                String statusText = "";
                if (app.getStatus() != null) {
                    switch (app.getStatus().toLowerCase()) {
                        case "submitted":
                            statusText = "Đã nộp";
                            break;
                        case "reviewing":
                            statusText = "Đang xem xét";
                            break;
                        case "accepted":
                            statusText = "Chấp nhận";
                            break;
                        case "denied":
                            statusText = "Từ chối";
                            break;
                        case "withdrawn":
                            statusText = "Rút hồ sơ";
                            break;
                        default:
                            statusText = app.getStatus();
                    }
                }
                cell6.setCellValue(statusText);
                cell6.setCellStyle(dataStyle);

                // CV/Tài liệu
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(app.getCvUrl() != null && !app.getCvUrl().isEmpty() ? "Có" : "Không có");
                cell7.setCellStyle(dataStyle);

                // Ghi chú
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(app.getNote() != null ? app.getNote() : "");
                cell8.setCellStyle(dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Set response headers
            String fileName = "danh_sach_ung_vien_" +
                    java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // Write workbook to response
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            System.err.println("❌ Error exporting applications to Excel: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi xuất file Excel");
        }
    }

    // Endpoint để xem chi tiết application (trả về modal content)
    @GetMapping("/applications/{id}/detail")
    public String viewApplicationDetail(
            @PathVariable Long id,
            Model model,
            HttpSession session) {
        try {
            if (!ensureAdmin(session)) {
                return "fragments/error :: error";
            }

            // Lấy application detail từ service
            Application application = applicationService.getApplicationsSimple(PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent()
                    .stream()
                    .filter(app -> app.getApplicationId() != null && app.getApplicationId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (application == null) {
                model.addAttribute("error", "Không tìm thấy đơn ứng tuyển");
                return "fragments/error :: error";
            }

            model.addAttribute("application", application);
            return "fragments/application-detail :: detail";
        } catch (Exception e) {
            System.err.println("❌ Error viewing application detail: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi tải chi tiết đơn ứng tuyển");
            return "fragments/error :: error";
        }
    }

    // Test endpoint để kiểm tra API có hoạt động không
    @GetMapping("/api/applications/test")
    @ResponseBody
    public Map<String, Object> testApplicationsApi(HttpSession session) {
        System.out.println("🧪 Test endpoint called");
        if (!ensureAdmin(session)) {
            return Map.of("success", false, "message", "Not admin");
        }

        long total = applicationRepository.count();
        System.out.println("📊 Total applications in DB: " + total);

        return Map.of(
                "success", true,
                "total", total,
                "message", "API is working"
        );
    }

    @GetMapping("/api/applications")
    @ResponseBody
    public Map<String, Object> getApplicationsApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFilter,
            HttpSession session) {

        System.out.println("🔍 API /admin/api/applications called - page: " + page + ", size: " + size);

        if (!ensureAdmin(session)) {
            System.out.println("❌ Not admin or not logged in");
            return Map.of("data", List.of(), "total", 0L, "page", 0, "totalPages", 0);
        }

        try {
            // Đảm bảo page và size hợp lệ
            if (page < 0) page = 0;
            if (size < 1) size = 10;

            Pageable pageable = PageRequest.of(page, size);
            Page<Application> applicationsPage = applicationService.getApplicationsSimple(pageable);

            System.out.println("📊 Total applications from DB: " + applicationsPage.getTotalElements());
            System.out.println("📄 Applications in page: " + applicationsPage.getContent().size());

            // Lọc theo search và status nếu có
            List<Application> applications = applicationsPage.getContent();

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                applications = applications.stream()
                        .filter(app ->
                                (app.getCandidateName() != null && app.getCandidateName().toLowerCase().contains(searchLower)) ||
                                        (app.getCandidateEmail() != null && app.getCandidateEmail().toLowerCase().contains(searchLower)) ||
                                        (app.getPosition() != null && app.getPosition().toLowerCase().contains(searchLower))
                        )
                        .collect(java.util.stream.Collectors.toList());
            }

            if (status != null && !status.trim().isEmpty()) {
                applications = applications.stream()
                        .filter(app -> status.equalsIgnoreCase(app.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Lọc theo dateFilter nếu có
            if (dateFilter != null && !dateFilter.trim().isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime filterDate = null;

                switch (dateFilter) {
                    case "today":
                        filterDate = now.minusDays(1);
                        break;
                    case "week":
                        filterDate = now.minusWeeks(1);
                        break;
                    case "month":
                        filterDate = now.minusMonths(1);
                        break;
                }

                if (filterDate != null) {
                    final LocalDateTime finalFilterDate = filterDate;
                    applications = applications.stream()
                            .filter(app -> app.getAppliedAt() != null && app.getAppliedAt().isAfter(finalFilterDate))
                            .collect(java.util.stream.Collectors.toList());
                }
            }

            System.out.println("✅ Applications after filter: " + applications.size());
            if (!applications.isEmpty()) {
                Application first = applications.get(0);
                System.out.println("📝 First application - ID: " + first.getApplicationId() +
                        ", Name: " + first.getCandidateName() +
                        ", Email: " + first.getCandidateEmail() +
                        ", Status: " + first.getStatus());
            }

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", applications);
            response.put("total", applicationsPage.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", applicationsPage.getTotalPages());

            System.out.println("📤 Sending response - total: " + response.get("total") + ", data size: " + applications.size());
            return response;
        } catch (Exception e) {
            System.err.println("❌ Error in getApplicationsApi: " + e.getMessage());
            e.printStackTrace();
            return Map.of("data", List.of(), "total", 0L, "page", 0, "totalPages", 0, "error", e.getMessage());
        }
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
    public String premium(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);

        // Xử lý pagination
        if (page < 0) page = 0;
        if (size < 1) size = 5;

        List<com.joblink.joblink.entity.PremiumPackages> allPackages = filterPackages(search, status);

        // Tính total và totalPages
        long total = allPackages.size();
        int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;

        // Paginate
        int start = page * size;
        int end = Math.min(start + size, (int) total);
        List<com.joblink.joblink.entity.PremiumPackages> packages = allPackages.subList(start, end);

        model.addAttribute("packages", packages);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("status", status != null ? status : "");
        model.addAttribute("total", total);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        return "premium";
    }

    // Endpoint trả về HTML fragment (chỉ table và pagination) cho AJAX
    @GetMapping("/premium/table")
    public String premiumTable(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpSession session) {
        try {
            if (!ensureAdmin(session)) {
                return "fragments/error :: error";
            }

            // Xử lý pagination
            if (page < 0) page = 0;
            if (size < 1) size = 5;

            List<com.joblink.joblink.entity.PremiumPackages> allPackages = filterPackages(search, status);

            // Tính total và totalPages
            long total = allPackages.size();
            int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;

            // Paginate
            int start = page * size;
            int end = Math.min(start + size, (int) total);
            List<com.joblink.joblink.entity.PremiumPackages> packages = allPackages.subList(start, end);

            model.addAttribute("packages", packages);
            model.addAttribute("search", search != null ? search : "");
            model.addAttribute("status", status != null ? status : "");
            model.addAttribute("total", total);
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("totalPages", totalPages);
            return "premium :: table";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            return "fragments/error :: error";
        }
    }

    // Helper method để filter packages
    private List<com.joblink.joblink.entity.PremiumPackages> filterPackages(String search, String status) {
        List<com.joblink.joblink.entity.PremiumPackages> packages = premiumPackageRepository.findAll();

        // Lọc theo search (tên hoặc code)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            packages = packages.stream()
                    .filter(pkg ->
                            (pkg.getName() != null && pkg.getName().toLowerCase().contains(searchLower)) ||
                                    (pkg.getCode() != null && pkg.getCode().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
        }

        // Lọc theo status (active/inactive)
        if (status != null && !status.trim().isEmpty()) {
            boolean isActive = "active".equalsIgnoreCase(status);
            packages = packages.stream()
                    .filter(pkg -> pkg.getIsActive() != null && pkg.getIsActive() == isActive)
                    .collect(java.util.stream.Collectors.toList());
        }

        return packages;
    }

    // Endpoint để lấy package theo ID (cho modal edit)
    @GetMapping("/premium/{id}")
    @ResponseBody
    public ResponseEntity<?> getPackageById(@PathVariable Integer id, HttpSession session) {
        if (!ensureAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            com.joblink.joblink.entity.PremiumPackages pkg = premiumPackageRepository.findById(id)
                    .orElse(null);
            if (pkg == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Package not found"));
            }
            return ResponseEntity.ok(pkg);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error loading package: " + e.getMessage()));
        }
    }

    // Endpoint để cập nhật package
    @PostMapping("/premium/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePackage(
            @PathVariable Integer id,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam String userType,
            @RequestParam java.math.BigDecimal price,
            @RequestParam Integer durationDays,
            @RequestParam(required = false) Integer maxActiveJobs,
            @RequestParam(required = false) Integer boostCredits,
            @RequestParam(required = false) Integer candidateViews,
            @RequestParam(required = false) Boolean highlight,
            @RequestParam(required = false, defaultValue = "false") Boolean cvTemplatesAccess,
            @RequestParam(required = false, defaultValue = "false") Boolean messagingEnabled,
            @RequestParam(required = false, defaultValue = "false") Boolean seekerNetworkingEnabled,
            @RequestParam(required = false) Integer pdfExportLimit,
            @RequestParam(required = false) String features,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            HttpSession session) {
        if (!ensureAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Unauthorized"));
        }
        try {
            com.joblink.joblink.entity.PremiumPackages pkg = premiumPackageRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Package not found"));

            // Kiểm tra code trùng lặp (chỉ khi code thay đổi)
            if (!pkg.getCode().equals(code)) {
                if (premiumPackageRepository.findByCodeAndIsActiveTrue(code).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "error", "Mã gói đã tồn tại"));
                }
            }

            pkg.setCode(code);
            pkg.setName(name);
            pkg.setUserType(userType);
            pkg.setPrice(price);
            pkg.setDurationDays(durationDays);
            pkg.setMaxActiveJobs(maxActiveJobs);
            pkg.setBoostCredits(boostCredits);
            pkg.setCandidateViews(candidateViews);
            pkg.setHighlight(highlight != null ? highlight : false);
            pkg.setCvTemplatesAccess(cvTemplatesAccess != null ? cvTemplatesAccess : false);
            pkg.setMessagingEnabled(messagingEnabled != null ? messagingEnabled : false);
            pkg.setSeekerNetworkingEnabled(seekerNetworkingEnabled != null ? seekerNetworkingEnabled : false);
            pkg.setPdfExportLimit(pdfExportLimit);
            pkg.setFeatures(features);
            pkg.setIsActive(isActive != null ? isActive : true);
            pkg.setUpdatedAt(LocalDateTime.now());

            premiumPackageRepository.save(pkg);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật gói thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Lỗi khi cập nhật: " + e.getMessage()));
        }
    }

    // Endpoint để tạo package mới
    @PostMapping("/premium/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPackage(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam String userType,
            @RequestParam java.math.BigDecimal price,
            @RequestParam Integer durationDays,
            @RequestParam(required = false) Integer maxActiveJobs,
            @RequestParam(required = false) Integer boostCredits,
            @RequestParam(required = false) Integer candidateViews,
            @RequestParam(required = false) Boolean highlight,
            @RequestParam(required = false, defaultValue = "false") Boolean cvTemplatesAccess,
            @RequestParam(required = false, defaultValue = "false") Boolean messagingEnabled,
            @RequestParam(required = false, defaultValue = "false") Boolean seekerNetworkingEnabled,
            @RequestParam(required = false) Integer pdfExportLimit,
            @RequestParam(required = false) String features,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            HttpSession session) {
        if (!ensureAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Unauthorized"));
        }
        try {
            // Kiểm tra code đã tồn tại chưa
            if (premiumPackageRepository.findByCodeAndIsActiveTrue(code).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Mã gói đã tồn tại"));
            }

            com.joblink.joblink.entity.PremiumPackages pkg = new com.joblink.joblink.entity.PremiumPackages();
            pkg.setCode(code);
            pkg.setName(name);
            pkg.setUserType(userType);
            pkg.setPrice(price);
            pkg.setDurationDays(durationDays);
            pkg.setMaxActiveJobs(maxActiveJobs);
            pkg.setBoostCredits(boostCredits);
            pkg.setCandidateViews(candidateViews);
            pkg.setHighlight(highlight != null ? highlight : false);
            pkg.setCvTemplatesAccess(cvTemplatesAccess != null ? cvTemplatesAccess : false);
            pkg.setMessagingEnabled(messagingEnabled != null ? messagingEnabled : false);
            pkg.setSeekerNetworkingEnabled(seekerNetworkingEnabled != null ? seekerNetworkingEnabled : false);
            pkg.setPdfExportLimit(pdfExportLimit);
            pkg.setFeatures(features);
            pkg.setIsActive(isActive != null ? isActive : true);
            pkg.setCreatedAt(LocalDateTime.now());
            pkg.setUpdatedAt(LocalDateTime.now());

            premiumPackageRepository.save(pkg);
            return ResponseEntity.ok(Map.of("success", true, "message", "Tạo gói thành công", "packageId", pkg.getPackageId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Lỗi khi tạo gói: " + e.getMessage()));
        }
    }

    // Endpoint để soft delete (toggle isActive)
    @PostMapping("/premium/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> softDeletePackage(@PathVariable Integer id, HttpSession session) {
        if (!ensureAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Unauthorized"));
        }
        try {
            com.joblink.joblink.entity.PremiumPackages pkg = premiumPackageRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Package not found"));

            // Toggle isActive (soft delete)
            pkg.setIsActive(!pkg.getIsActive());
            pkg.setUpdatedAt(LocalDateTime.now());
            premiumPackageRepository.save(pkg);

            String action = pkg.getIsActive() ? "khôi phục" : "xóa";
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã " + action + " gói thành công", "isActive", pkg.getIsActive()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Lỗi khi xóa: " + e.getMessage()));
        }
    }

    @GetMapping("/payments")
    public String payments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);

        // Xử lý pagination
        if (page < 0) page = 0;
        if (size < 1) size = 5;

        List<java.util.Map<String, Object>> allPaymentData = getPaymentData(search, status);

        // Tính total và totalPages
        long total = allPaymentData.size();
        int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;

        // Paginate
        int start = page * size;
        int end = Math.min(start + size, (int) total);
        List<java.util.Map<String, Object>> paymentData = allPaymentData.subList(start, end);

        model.addAttribute("payments", paymentData);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("status", status != null ? status : "");
        model.addAttribute("total", total);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        return "payment";
    }

    // Endpoint trả về HTML fragment (chỉ table) cho AJAX
    @GetMapping("/payments/table")
    public String paymentsTable(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpSession session) {
        try {
            if (!ensureAdmin(session)) {
                return "fragments/error :: error";
            }

            // Xử lý pagination
            if (page < 0) page = 0;
            if (size < 1) size = 5;

            List<java.util.Map<String, Object>> allPaymentData = getPaymentData(search, status);

            // Tính total và totalPages
            long total = allPaymentData.size();
            int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;

            // Paginate
            int start = page * size;
            int end = Math.min(start + size, (int) total);
            List<java.util.Map<String, Object>> paymentData = allPaymentData.subList(start, end);

            model.addAttribute("payments", paymentData);
            model.addAttribute("search", search != null ? search : "");
            model.addAttribute("status", status != null ? status : "");
            model.addAttribute("total", total);
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("totalPages", totalPages);
            return "payment :: table";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            return "fragments/error :: error";
        }
    }

    // Helper method để lấy và filter payment data
    private List<java.util.Map<String, Object>> getPaymentData(String search, String status) {
        // Lấy tất cả invoices và join với payment, employer
        List<Invoice> allInvoices = invoiceRepository.findAll();
        List<java.util.Map<String, Object>> paymentData = new java.util.ArrayList<>();

        for (Invoice invoice : allInvoices) {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("invoiceId", invoice.getInvoiceId());
            data.put("amount", invoice.getAmount());
            data.put("status", invoice.getStatus());
            data.put("issuedAt", invoice.getIssuedAt());

            // Lấy Payment nếu có
            Payment payment = paymentRepository.findByInvoiceId(invoice.getInvoiceId()).orElse(null);
            if (payment != null) {
                data.put("provider", payment.getProvider());
                data.put("txRef", payment.getTxRef());
            } else {
                data.put("provider", null);
                data.put("txRef", null);
            }

            // Lấy Employer nếu có employerId
            String companyName = "N/A";
            if (invoice.getEmployerId() != null) {
                Employer employer = employerRepository.findById(invoice.getEmployerId().longValue()).orElse(null);
                if (employer != null) {
                    companyName = employer.getCompanyName();
                }
            } else if (invoice.getSeekerId() != null) {
                // Nếu có seekerId, lấy tên từ JobSeekerProfile
                JobSeekerProfile seeker = jobSeekerRepo.findById(invoice.getSeekerId()).orElse(null);
                if (seeker != null && seeker.getFullName() != null && !seeker.getFullName().trim().isEmpty()) {
                    companyName = seeker.getFullName();
                } else if (invoice.getUserId() != null) {
                    // Nếu không có fullName, lấy email từ User
                    User user = userRepository.findById(invoice.getUserId()).orElse(null);
                    if (user != null) {
                        companyName = user.getEmail();
                    }
                }
            } else if (invoice.getUserId() != null) {
                // Nếu không có employerId và seekerId, lấy email từ User
                User user = userRepository.findById(invoice.getUserId()).orElse(null);
                if (user != null) {
                    companyName = user.getEmail();
                }
            }
            data.put("companyName", companyName);

            paymentData.add(data);
        }

        // Lọc theo search (mã hóa đơn hoặc tên công ty)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            paymentData = paymentData.stream()
                    .filter(p -> {
                        String invoiceIdStr = String.valueOf(p.get("invoiceId"));
                        String companyNameStr = (String) p.get("companyName");
                        return invoiceIdStr.contains(searchLower) ||
                                (companyNameStr != null && companyNameStr.toLowerCase().contains(searchLower));
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        // Lọc theo status
        if (status != null && !status.trim().isEmpty()) {
            String statusUpper = status.toUpperCase();
            // Map các giá trị từ select sang status trong DB
            String dbStatus = statusUpper;
            if ("unpaid".equalsIgnoreCase(status)) {
                dbStatus = "PENDING";
            } else if ("paid".equalsIgnoreCase(status)) {
                dbStatus = "PAID";
            } else if ("failed".equalsIgnoreCase(status)) {
                dbStatus = "CANCELLED";
            }

            final String finalStatus = dbStatus;
            paymentData = paymentData.stream()
                    .filter(p -> {
                        String pStatus = (String) p.get("status");
                        return pStatus != null && pStatus.equalsIgnoreCase(finalStatus);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        // Sắp xếp theo mã hóa đơn tăng dần
        paymentData.sort((a, b) -> {
            Integer idA = (Integer) a.get("invoiceId");
            Integer idB = (Integer) b.get("invoiceId");
            if (idA == null && idB == null) return 0;
            if (idA == null) return 1;
            if (idB == null) return -1;
            return idA.compareTo(idB);
        });

        return paymentData;
    }

    @GetMapping("/feedbacks")
    public String feedbacks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);

        // Xử lý pagination
        if (page < 0) page = 0;
        if (size < 1) size = 5;

        // Lấy và lọc đánh giá
        List<com.joblink.joblink.entity.CompanyReview> allReviews;
        try {
            allReviews = companyReviewRepository.findAllWithSeekerAndEmployer();
            // Set isDeleted = false mặc định nếu null (vì @Transient)
            for (com.joblink.joblink.entity.CompanyReview review : allReviews) {
                if (review.getIsDeleted() == null) {
                    review.setIsDeleted(false);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading reviews: " + e.getMessage());
            e.printStackTrace();
            allReviews = new java.util.ArrayList<>();
        }
        List<com.joblink.joblink.entity.CompanyReview> filteredReviews = filterReviews(allReviews, search, rating);

        // Tính total và totalPages
        long total = filteredReviews.size();
        int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;

        // Paginate
        int start = page * size;
        int end = Math.min(start + size, (int) total);
        List<com.joblink.joblink.entity.CompanyReview> reviews = start < filteredReviews.size()
                ? filteredReviews.subList(start, end)
                : new java.util.ArrayList<>();

        model.addAttribute("reviews", reviews);
        model.addAttribute("total", total);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("rating", rating != null ? rating : "");

        return "feedbacks";
    }

    // Endpoint trả về HTML fragment (chỉ table và pagination) cho AJAX
    @GetMapping("/feedbacks/table")
    public String feedbacksTable(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpSession session) {
        try {
            if (!ensureAdmin(session)) {
                return "fragments/error :: error";
            }

            // Xử lý pagination
            if (page < 0) page = 0;
            if (size < 1) size = 5;

            // Lấy và lọc đánh giá
            List<com.joblink.joblink.entity.CompanyReview> allReviews;
            try {
                allReviews = companyReviewRepository.findAllWithSeekerAndEmployer();
                // Set isDeleted = false mặc định nếu null (vì @Transient)
                for (com.joblink.joblink.entity.CompanyReview review : allReviews) {
                    if (review.getIsDeleted() == null) {
                        review.setIsDeleted(false);
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading reviews: " + e.getMessage());
                e.printStackTrace();
                allReviews = new java.util.ArrayList<>();
            }
            List<com.joblink.joblink.entity.CompanyReview> filteredReviews = filterReviews(allReviews, search, rating);

            // Tính total và totalPages
            long total = filteredReviews.size();
            int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;

            // Paginate
            int start = page * size;
            int end = Math.min(start + size, (int) total);
            List<com.joblink.joblink.entity.CompanyReview> reviews = start < filteredReviews.size()
                    ? filteredReviews.subList(start, end)
                    : new java.util.ArrayList<>();

            model.addAttribute("reviews", reviews);
            model.addAttribute("total", total);
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("search", search != null ? search : "");
            model.addAttribute("rating", rating != null ? rating : "");

            return "feedbacks :: table";
        } catch (Exception e) {
            System.err.println("❌ Error in feedbacksTable endpoint: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("reviews", new java.util.ArrayList<>());
            model.addAttribute("total", 0);
            model.addAttribute("currentPage", 0);
            model.addAttribute("size", 5);
            model.addAttribute("totalPages", 0);
            return "feedbacks :: table";
        }
    }

    // Helper method để filter reviews
    private List<com.joblink.joblink.entity.CompanyReview> filterReviews(
            List<com.joblink.joblink.entity.CompanyReview> allReviews,
            String search,
            String rating) {

        List<com.joblink.joblink.entity.CompanyReview> filtered = new java.util.ArrayList<>(allReviews);

        // Lọc theo search (tên seeker, email seeker, tên công ty, nội dung comment)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(review -> {
                        boolean match = false;
                        // Tìm theo tên seeker
                        if (review.getSeeker() != null && review.getSeeker().getFullName() != null) {
                            match = match || review.getSeeker().getFullName().toLowerCase().contains(searchLower);
                        }
                        // Tìm theo email seeker
                        if (review.getSeeker() != null && review.getSeeker().getEmail() != null) {
                            match = match || review.getSeeker().getEmail().toLowerCase().contains(searchLower);
                        }
                        // Tìm theo tên công ty
                        if (review.getEmployer() != null && review.getEmployer().getCompanyName() != null) {
                            match = match || review.getEmployer().getCompanyName().toLowerCase().contains(searchLower);
                        }
                        // Tìm theo nội dung comment
                        if (review.getComment() != null) {
                            match = match || review.getComment().toLowerCase().contains(searchLower);
                        }
                        return match;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        // Lọc theo rating
        if (rating != null && !rating.trim().isEmpty()) {
            try {
                Byte ratingValue = Byte.parseByte(rating);
                final Byte finalRating = ratingValue;
                filtered = filtered.stream()
                        .filter(review -> review.getRating() != null && review.getRating().equals(finalRating))
                        .collect(java.util.stream.Collectors.toList());
            } catch (NumberFormatException e) {
                // Nếu không parse được, bỏ qua filter này
            }
        }

        return filtered;
    }

    // Endpoint để xem chi tiết review (trả về modal content)
    @GetMapping("/feedbacks/{id}/detail")
    public String viewReviewDetail(
            @PathVariable Long id,
            Model model,
            HttpSession session) {
        try {
            if (!ensureAdmin(session)) {
                return "fragments/error :: error";
            }

            com.joblink.joblink.entity.CompanyReview review = companyReviewRepository.findById(id).orElse(null);

            if (review == null) {
                model.addAttribute("error", "Không tìm thấy đánh giá");
                return "fragments/error :: error";
            }

            model.addAttribute("review", review);
            return "feedbacks :: detail";
        } catch (Exception e) {
            System.err.println("❌ Error viewing review detail: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi tải chi tiết đánh giá");
            return "fragments/error :: error";
        }
    }

    // Endpoint để soft delete review
    @PostMapping("/feedbacks/{id}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> softDeleteReview(
            @PathVariable Long id,
            HttpSession session) {
        if (!ensureAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Không có quyền truy cập"));
        }

        try {
            com.joblink.joblink.entity.CompanyReview review = companyReviewRepository.findById(id).orElse(null);

            if (review == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy đánh giá"));
            }

            // Soft delete: set isDeleted = true
            review.setIsDeleted(true);

            // Thử update vào DB nếu column tồn tại
            try {
                int updated = companyReviewRepository.updateIsDeleted(id, true);
                if (updated > 0) {
                    System.out.println("✅ Đã cập nhật is_deleted trong DB cho review ID: " + id);
                } else {
                    System.out.println("⚠️ Không thể update is_deleted (có thể column chưa tồn tại), chỉ đánh dấu trong memory");
                }
            } catch (Exception e) {
                // Nếu column chưa tồn tại, chỉ đánh dấu trong memory
                System.out.println("⚠️ Column is_deleted chưa tồn tại, chỉ đánh dấu trong memory: " + e.getMessage());
            }

            System.out.println("✅ Đã xóa mềm review ID: " + id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa đánh giá thành công"));
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa mềm review: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/statistic")
    public String statistic(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);

        // Lấy dữ liệu thống kê tổng quan
        long totalUsers = userService.getTotalUsers();
        long totalCompanies = employerRepository.countAllEmployerIds();
        long totalCVs = jobSeekerRepo.count();
        long totalJobs = dashboardService.countJobPosts();
        long totalApplications = dashboardService.countApplications();
        long totalPremiumPackages = premiumPackageRepository.count();
        long totalInvoices = invoiceRepository.count();
        double totalRevenue = dashboardService.getTotalRevenue();
        String formattedRevenue = CurrencyUtils.formatVND(totalRevenue);

        // Thêm vào model
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCompanies", totalCompanies);
        model.addAttribute("totalCVs", totalCVs);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("totalPremiumPackages", totalPremiumPackages);
        model.addAttribute("totalInvoices", totalInvoices);
        model.addAttribute("totalRevenue", formattedRevenue);

        return "statistic";
    }

    @GetMapping("/api/statistics/users-by-month")
    @ResponseBody
    public Map<String, Object> getUsersByMonth(HttpSession session) {
        if (!ensureAdmin(session)) {
            return Map.of("months", new String[0], "counts", new long[0]);
        }

        try {
            List<User> allUsers = userRepository.findAll();
            Map<Integer, Long> usersByMonth = new HashMap<>();

            // Khởi tạo tất cả 12 tháng với giá trị 0
            for (int i = 1; i <= 12; i++) {
                usersByMonth.put(i, 0L);
            }

            // Đếm người dùng theo tháng
            for (User user : allUsers) {
                if (user.getCreatedAt() != null) {
                    int month = user.getCreatedAt().getMonthValue();
                    usersByMonth.put(month, usersByMonth.get(month) + 1);
                }
            }

            String[] months = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
            long[] counts = new long[12];
            for (int i = 0; i < 12; i++) {
                counts[i] = usersByMonth.get(i + 1);
            }

            return Map.of("months", months, "counts", counts);
        } catch (Exception e) {
            e.printStackTrace();
            String[] months = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
            return Map.of("months", months, "counts", new long[12]);
        }
    }

    @GetMapping("/api/statistics/revenue-by-month")
    @ResponseBody
    public Map<String, Object> getRevenueByMonth(HttpSession session) {
        if (!ensureAdmin(session)) {
            return Map.of("months", new String[0], "revenues", new double[0]);
        }

        try {
            List<Payment> allPayments = paymentRepository.findAll();
            Map<Integer, Double> revenueByMonth = new HashMap<>();

            // Khởi tạo tất cả 12 tháng với giá trị 0
            for (int i = 1; i <= 12; i++) {
                revenueByMonth.put(i, 0.0);
            }

            // Tính doanh thu theo tháng (chỉ tính các payment thành công)
            for (Payment payment : allPayments) {
                if (payment.getCreatedAt() != null &&
                        (payment.getStatus().equals("SUCCESS") || payment.getStatus().equals("PAID"))) {
                    int month = payment.getCreatedAt().getMonthValue();
                    double amount = payment.getAmount() != null ? payment.getAmount().doubleValue() : 0.0;
                    revenueByMonth.put(month, revenueByMonth.get(month) + amount);
                }
            }

            String[] months = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
            double[] revenues = new double[12];
            for (int i = 0; i < 12; i++) {
                revenues[i] = revenueByMonth.get(i + 1);
            }

            return Map.of("months", months, "revenues", revenues);
        } catch (Exception e) {
            e.printStackTrace();
            String[] months = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
            return Map.of("months", months, "revenues", new double[12]);
        }
    }

    @GetMapping("/api/statistics/applications-by-industry")
    @ResponseBody
    public Map<String, Object> getApplicationsByIndustry(HttpSession session) {
        if (!ensureAdmin(session)) {
            return Map.of("labels", new String[0], "counts", new long[0]);
        }

        try {
            List<Application> allApplications = applicationRepository.findAll();
            Map<String, Long> applicationsByCategory = new HashMap<>();

            // Đếm ứng tuyển theo category/ngành
            for (Application application : allApplications) {
                try {
                    com.joblink.joblink.entity.JobPosting job = jobPostingRepository.findById(Long.valueOf(application.getJobId())).orElse(null);
                    if (job != null && job.getCategory() != null) {
                        String categoryName = job.getCategory().getName() != null ?
                                job.getCategory().getName() : "Khác";
                        applicationsByCategory.put(categoryName,
                                applicationsByCategory.getOrDefault(categoryName, 0L) + 1);
                    } else {
                        applicationsByCategory.put("Khác",
                                applicationsByCategory.getOrDefault("Khác", 0L) + 1);
                    }
                } catch (Exception e) {
                    applicationsByCategory.put("Khác",
                            applicationsByCategory.getOrDefault("Khác", 0L) + 1);
                }
            }

            // Sắp xếp theo số lượng giảm dần và lấy top 10
            List<Map.Entry<String, Long>> sorted = applicationsByCategory.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            String[] labels = new String[sorted.size()];
            long[] counts = new long[sorted.size()];

            for (int i = 0; i < sorted.size(); i++) {
                labels[i] = sorted.get(i).getKey();
                counts[i] = sorted.get(i).getValue();
            }

            // Nếu không có dữ liệu, trả về mảng rỗng
            if (labels.length == 0) {
                return Map.of("labels", new String[]{"Chưa có dữ liệu"}, "counts", new long[]{0});
            }

            return Map.of("labels", labels, "counts", counts);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", new String[]{"Lỗi tải dữ liệu"}, "counts", new long[]{0});
        }
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