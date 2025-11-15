package com.joblink.joblink.controller;

import com.joblink.joblink.dto.ApplicationFilter;
import com.joblink.joblink.entity.Application;
import com.joblink.joblink.service.ApplicationService;
import com.joblink.joblink.service.ApplicationService2;
import com.joblink.joblink.service.SessionHelperService;
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
    private final SessionHelperService sessionHelperService; // ← THÊM DÒNG NÀY

    // ← UPDATE CONSTRUCTOR - THÊM PARAMETER
    public EmployerApplicationController(ApplicationService applicationService,
                                         SessionHelperService sessionHelperService) {
        this.applicationService = applicationService;
        this.sessionHelperService = sessionHelperService;
    }

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

        // THAY THẾ: Kiểm tra authentication
        if (!sessionHelperService.isEmployer()) {
            return "redirect:/signin";
        }

        ApplicationFilter filter = createFilter(search, positions, minExperience,
                maxExperience, statuses, educationLevels,
                location, page, size);

        // THAY THẾ 1L: Lấy employerId thực từ session
        Long employerId = sessionHelperService.getCurrentEmployerId();

        Page<Application> applications = applicationService.getApplicationsWithFilters(filter, employerId);
        Map<String, Object> filterOptions = applicationService.getFilterOptions();

        addModelAttributes(model, applications, filterOptions, filter, size, "all");

        return "employer/employer-applications";
    }

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

        // THAY THẾ: Kiểm tra authentication
        if (!sessionHelperService.isEmployer()) {
            return "redirect:/signin";
        }

        ApplicationFilter filter = createFilter(search, positions, minExperience,
                maxExperience, statuses, educationLevels,
                location, page, size);

        // THAY THẾ 1L: Lấy employerId thực từ session
        Long employerId = sessionHelperService.getCurrentEmployerId();

        Page<Application> applications = applicationService.getSavedApplicationsWithFilters(filter, employerId);
        Map<String, Object> filterOptions = applicationService.getFilterOptions();

        addModelAttributes(model, applications, filterOptions, filter, size, "saved");

        return "employer/employer-saved";
    }

    @PostMapping("/applications/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Map<String, Object> response = new HashMap<>();
        try {
            // THÊM: Kiểm tra authentication
            if (!sessionHelperService.isEmployer()) {
                response.put("success", false);
                response.put("message", "Authentication failed: Not an employer");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Application updatedApplication = applicationService.updateApplicationStatus(id, status);

            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("newStatus", status);
            response.put("applicationId", id);
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/applications/{id}/bookmark")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleBookmark(@PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        try {
            // THAY THẾ 1L: Lấy employerId thực từ session
            Long employerId = sessionHelperService.getCurrentEmployerId();

            boolean isBookmarked = applicationService.toggleBookmark(id, employerId);

            response.put("success", true);
            response.put("message", isBookmarked ? "Application bookmarked" : "Bookmark removed");
            response.put("isBookmarked", isBookmarked);
            response.put("applicationId", id);
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to toggle bookmark: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== HELPER METHODS (GIỮ NGUYÊN) ==========

    private ApplicationFilter createFilter(String search, List<String> positions,
                                           Integer minExperience, Integer maxExperience,
                                           List<String> statuses, List<String> educationLevels,
                                           String location, int page, int size) {
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
        return filter;
    }

    private void addModelAttributes(Model model, Page<Application> applications,
                                    Map<String, Object> filterOptions, ApplicationFilter filter,
                                    int size, String pageType) {
        model.addAttribute("applications", applications.getContent());
        model.addAttribute("currentPage", applications.getNumber());
        model.addAttribute("totalPages", applications.getTotalPages());
        model.addAttribute("totalElements", applications.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("pageType", pageType);

        model.addAttribute("positions", filterOptions.getOrDefault("positions", new ArrayList<>()));
        model.addAttribute("locations", filterOptions.getOrDefault("locations", new ArrayList<>()));
        model.addAttribute("educationLevels", filterOptions.getOrDefault("educationLevels", new ArrayList<>()));
        model.addAttribute("statuses", filterOptions.getOrDefault("statuses", new ArrayList<>()));

        model.addAttribute("search", filter.getSearch());
        model.addAttribute("selectedPositions", filter.getPositions());
        model.addAttribute("minExperience", filter.getMinExperience() != null ? filter.getMinExperience() : 0);
        model.addAttribute("maxExperience", filter.getMaxExperience() != null ? filter.getMaxExperience() : 20);
        model.addAttribute("selectedStatuses", filter.getStatuses());
        model.addAttribute("selectedEducationLevels", filter.getEducationLevels());
        model.addAttribute("location", filter.getLocation());
    }

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

    /**
     * XÓA METHOD NÀY - KHÔNG CẦN NỮA
     */
    // private Long getCurrentEmployerId() {
    //     return 1L; // DELETE THIS METHOD
    // }
}