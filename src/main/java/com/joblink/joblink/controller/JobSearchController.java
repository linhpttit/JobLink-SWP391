package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.service.JobSearchService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobseeker")
public class JobSearchController {
    private final JobSearchService jobSearchService;

    public JobSearchController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @GetMapping("/search")
    public String searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer provinceId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        // Get search results
        Map<String, Object> searchResults = jobSearchService.searchJobs(
                keyword, provinceId, categoryId, page, pageSize
        );

        // Get top company with most jobs
        Map<String, Object> topCompany = jobSearchService.getTopCompanyByJobCount();

        // Get all provinces for location filter
        List<Map<String, Object>> provinces = jobSearchService.getAllProvinces();

        // Get all categories for category filter
        List<Map<String, Object>> categories = jobSearchService.getAllCategories();

        model.addAttribute("user", user);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("topCompany", topCompany);
        model.addAttribute("provinces", provinces);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("keyword", keyword);
        model.addAttribute("provinceId", provinceId);
        model.addAttribute("categoryId", categoryId);

        return "job-search";
    }

    @GetMapping("/api/job-suggestions")
    @ResponseBody
    public ResponseEntity<List<String>> getJobSuggestions(@RequestParam String query) {
        List<String> suggestions = jobSearchService.getJobTitleSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/api/districts")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getDistrictsByProvince(
            @RequestParam Integer provinceId) {
        List<Map<String, Object>> districts = jobSearchService.getDistrictsByProvince(provinceId);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/api/jobs-by-filters")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getJobsByFilters(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer provinceId,
            @RequestParam(required = false) Integer districtId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String workType,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Integer maxSalary,
            @RequestParam(required = false) String experience,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize) {

        Map<String, Object> results = jobSearchService.searchJobsWithAdvancedFilters(
                keyword, provinceId, districtId, categoryId, workType,
                minSalary, maxSalary, experience, page, pageSize
        );
        return ResponseEntity.ok(results);
    }

    @GetMapping("/job-detail/{jobId}")
    public String getJobDetail(
            @PathVariable Integer jobId,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        Map<String, Object> jobDetail = jobSearchService.getJobDetailById(jobId);
        if (jobDetail == null) {
            return "redirect:/jobseeker/search";
        }

        model.addAttribute("user", user);
        model.addAttribute("job", jobDetail);

        return "job-detail";
    }

    @GetMapping("/api/live-suggestions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLiveSuggestions(@RequestParam String query) {
        Map<String, Object> suggestions = jobSearchService.getLiveSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/api/experience-levels")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getExperienceLevels() {
        List<Map<String, Object>> levels = jobSearchService.getExperienceLevels();
        return ResponseEntity.ok(levels);
    }

    @GetMapping("/api/work-types")
    @ResponseBody
    public ResponseEntity<List<String>> getWorkTypes() {
        List<String> workTypes = jobSearchService.getWorkTypes();
        return ResponseEntity.ok(workTypes);
    }

    @GetMapping("/api/salary-range")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSalaryRange() {
        Map<String, Object> salaryRange = jobSearchService.getSalaryRange();
        return ResponseEntity.ok(salaryRange);
    }

    @GetMapping("/api/search-comprehensive")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchComprehensive(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer provinceId,
            @RequestParam(required = false) Integer districtId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String workType,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Integer maxSalary,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) String educationLevel,
            @RequestParam(required = false) List<String> seekerSkills,
            @RequestParam(required = false) Integer seekerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize) {

        Map<String, Object> results = jobSearchService.searchJobsByComprehensiveFilters(
                keyword, provinceId, districtId, categoryId, workType,
                minSalary, maxSalary, experienceLevel, educationLevel,
                seekerSkills, seekerId, page, pageSize
        );
        return ResponseEntity.ok(results);
    }

    @GetMapping("/api/provinces")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getProvinces() {
        List<Map<String, Object>> provinces = jobSearchService.getAllProvinces();
        return ResponseEntity.ok(provinces);
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = jobSearchService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
}
