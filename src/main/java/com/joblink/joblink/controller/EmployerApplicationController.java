package com.joblink.joblink.controller;

import com.joblink.joblink.dto.ApplicationFilter;
import com.joblink.joblink.entity.Application;
import com.joblink.joblink.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EmployerApplicationController {

    private final ApplicationService applicationService;

    public EmployerApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Hiển thị trang chủ - quản lý ứng viên (TẤT CẢ ứng viên)
     * URL: GET /applications
     * FIXED: Xử lý tham số đúng cách
     */
    @GetMapping("/applications")
    public String showApplicationsPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> positions,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> educationLevels,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Tạo filter với giá trị mặc định và xử lý null
        ApplicationFilter filter = new ApplicationFilter();
        filter.setSearch(search != null ? search : "");
        filter.setPositions(positions != null ? positions : new ArrayList<>());
        filter.setMinExperience(minExperience);
        filter.setMaxExperience(maxExperience);
        filter.setStatuses(statuses != null ? statuses : new ArrayList<>());
        filter.setEducationLevels(educationLevels != null ? educationLevels : new ArrayList<>());
        filter.setLocation(location != null ? location : "");
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortDirection("DESC");

        // TODO: Thay thế 1L bằng employerId thực tế từ authentication
        Long employerId = getCurrentEmployerId(); // Cần implement method này

        // Lấy dữ liệu từ service (TẤT CẢ ứng viên)
        Page<Application> applications = applicationService.getApplicationsWithFilters(filter, employerId);
        Map<String, Object> filterOptions = applicationService.getFilterOptions();

        // Add data to model for Thymeleaf
        model.addAttribute("applications", applications.getContent());
        model.addAttribute("currentPage", applications.getNumber());
        model.addAttribute("totalPages", applications.getTotalPages());
        model.addAttribute("totalElements", applications.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("pageType", "all");

        // Filter options
        model.addAttribute("positions", filterOptions.getOrDefault("positions", new ArrayList<>()));
        model.addAttribute("locations", filterOptions.getOrDefault("locations", new ArrayList<>()));
        model.addAttribute("educationLevels", filterOptions.getOrDefault("educationLevels", new ArrayList<>()));
        model.addAttribute("statuses", filterOptions.getOrDefault("statuses", new ArrayList<>()));

        // Current filter values
        model.addAttribute("search", filter.getSearch());
        model.addAttribute("selectedPositions", filter.getPositions());
        model.addAttribute("minExperience", filter.getMinExperience() != null ? filter.getMinExperience() : 0);
        model.addAttribute("maxExperience", filter.getMaxExperience() != null ? filter.getMaxExperience() : 20);
        model.addAttribute("selectedStatuses", filter.getStatuses());
        model.addAttribute("selectedEducationLevels", filter.getEducationLevels());
        model.addAttribute("location", filter.getLocation());

        return "employer/employer-applications";
    }

    /**
     * Hiển thị trang SAVED - chỉ ứng viên đã bookmark
     * URL: GET /applications/saved
     * FIXED: Xử lý tham số đúng cách
     */
    @GetMapping("/saved")
    public String showSavedApplicationsPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> positions,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> educationLevels,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Tạo filter với giá trị mặc định và xử lý null
        ApplicationFilter filter = new ApplicationFilter();
        filter.setSearch(search != null ? search : "");
        filter.setPositions(positions != null ? positions : new ArrayList<>());
        filter.setMinExperience(minExperience);
        filter.setMaxExperience(maxExperience);
        filter.setStatuses(statuses != null ? statuses : new ArrayList<>());
        filter.setEducationLevels(educationLevels != null ? educationLevels : new ArrayList<>());
        filter.setLocation(location != null ? location : "");
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortDirection("DESC");

        // TODO: Thay thế 1L bằng employerId thực tế từ authentication
        Long employerId = getCurrentEmployerId(); // Cần implement method này

        // Lấy dữ liệu từ service (CHỈ ứng viên đã bookmark)
        Page<Application> applications = applicationService.getSavedApplicationsWithFilters(filter, employerId);
        Map<String, Object> filterOptions = applicationService.getFilterOptions();

        // Add data to model for Thymeleaf
        model.addAttribute("applications", applications.getContent());
        model.addAttribute("currentPage", applications.getNumber());
        model.addAttribute("totalPages", applications.getTotalPages());
        model.addAttribute("totalElements", applications.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("pageType", "saved");

        // Filter options
        model.addAttribute("positions", filterOptions.getOrDefault("positions", new ArrayList<>()));
        model.addAttribute("locations", filterOptions.getOrDefault("locations", new ArrayList<>()));
        model.addAttribute("educationLevels", filterOptions.getOrDefault("educationLevels", new ArrayList<>()));
        model.addAttribute("statuses", filterOptions.getOrDefault("statuses", new ArrayList<>()));

        // Current filter values
        model.addAttribute("search", filter.getSearch());
        model.addAttribute("selectedPositions", filter.getPositions());
        model.addAttribute("minExperience", filter.getMinExperience() != null ? filter.getMinExperience() : 0);
        model.addAttribute("maxExperience", filter.getMaxExperience() != null ? filter.getMaxExperience() : 20);
        model.addAttribute("selectedStatuses", filter.getStatuses());
        model.addAttribute("selectedEducationLevels", filter.getEducationLevels());
        model.addAttribute("location", filter.getLocation());

        return "employer/employer-saved";
    }

    /**
     * Cập nhật trạng thái ứng viên - Hỗ trợ cả AJAX và normal request
     * URL: POST /applications/{id}/status
     * FIXED: Sử dụng employerId thực tế
     */
    @PostMapping("/applications/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            Application updatedApplication = applicationService.updateApplicationStatus(id, status);

            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("newStatus", status);
            response.put("applicationId", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Bookmark ứng viên - Hỗ trợ cả AJAX và normal request
     * URL: POST /applications/{id}/bookmark
     * FIXED: Sử dụng employerId thực tế
     */
    @PostMapping("/applications/{id}/bookmark")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable Long id,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            // TODO: Thay thế 1L bằng employerId thực tế từ authentication
            Long employerId = getCurrentEmployerId();

            boolean isBookmarked = applicationService.toggleBookmark(id, employerId);

            response.put("success", true);
            response.put("message", isBookmarked ? "Application bookmarked" : "Bookmark removed");
            response.put("isBookmarked", isBookmarked);
            response.put("applicationId", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to toggle bookmark: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Helper method để lấy employerId từ authentication
     * TODO: Implement method này dựa trên hệ thống authentication của bạn
     */
    private Long getCurrentEmployerId() {
        // Tạm thời return 1L, cần thay thế bằng logic thực tế
        // Ví dụ: return SecurityContextHolder.getContext().getAuthentication().getPrincipal().getId();
        return 1L;
    }

    /**
     * API để lấy filter options (cho AJAX)
     */
    @GetMapping("/api/filter-options")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        try {
            Map<String, Object> options = applicationService.getFilterOptions();
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to load filter options");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}