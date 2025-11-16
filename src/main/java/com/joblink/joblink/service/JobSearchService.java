package com.joblink.joblink.service;

import com.joblink.joblink.dao.JobSearchDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JobSearchService {
    private final JobSearchDao jobSearchDao;

    public JobSearchService(JobSearchDao jobSearchDao) {
        this.jobSearchDao = jobSearchDao;
    }

    public Map<String, Object> searchJobs(String keyword, Integer provinceId,
                                          Integer categoryId, int page, int pageSize) {
        return jobSearchDao.searchJobs(keyword, provinceId, categoryId, page, pageSize);
    }

    public List<String> getJobTitleSuggestions(String query) {
        return jobSearchDao.getJobTitleSuggestions(query);
    }

    public List<Map<String, Object>> getAllProvinces() {
        return jobSearchDao.getAllProvinces();
    }

    public List<Map<String, Object>> getAllCategories() {
        return jobSearchDao.getAllCategories();
    }

    public List<Map<String, Object>> getDistrictsByProvince(Integer provinceId) {
        return jobSearchDao.getDistrictsByProvince(provinceId);
    }

    public Map<String, Object> getTopCompanyByJobCount() {
        return jobSearchDao.getTopCompanyByJobCount();
    }

    public Map<String, Object> searchJobsWithAdvancedFilters(
            String keyword, Integer provinceId, Integer districtId, Integer categoryId,
            String workType, Integer minSalary, Integer maxSalary, String experience,
            int page, int pageSize) {
        return jobSearchDao.searchJobsWithAdvancedFilters(
                keyword, provinceId, districtId, categoryId, workType,
                minSalary, maxSalary, experience, page, pageSize
        );
    }

    public Map<String, Object> getJobDetailById(Integer jobId) {
        return jobSearchDao.getJobDetailById(jobId);
    }

    public Map<String, Object> getLiveSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Map.of("skills", List.of(), "companies", List.of());
        }
        return jobSearchDao.getLiveSuggestions(query.trim());
    }

    public List<Map<String, Object>> getExperienceLevels() {
        return jobSearchDao.getExperienceLevels();
    }

    public List<String> getWorkTypes() {
        return jobSearchDao.getWorkTypes();
    }

    public Map<String, Object> getSalaryRange() {
        return jobSearchDao.getSalaryRange();
    }

    public Map<String, Object> searchJobsByComprehensiveFilters(
            String keyword,
            Integer provinceId,
            Integer districtId,
            Integer categoryId,
            String workType,
            Integer minSalary,
            Integer maxSalary,
            String experienceLevel,
            String educationLevel,
            List<String> seekerSkills,
            Integer seekerId,
            int page,
            int pageSize) {
        return jobSearchDao.searchJobsByComprehensiveFilters(
                keyword, provinceId, districtId, categoryId, workType,
                minSalary, maxSalary, experienceLevel, educationLevel,
                seekerSkills, seekerId, page, pageSize
        );
    }
}
