package com.joblink.joblink.service;

import com.joblink.joblink.auth.model.JobPosting;
import com.joblink.joblink.auth.model.JobSearchResult;
import com.joblink.joblink.dao.JobDAO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JobService {

    // Chỉ cần inject JobDAO
    private final JobDAO jobDAO;

    public JobService(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    // Phương thức này bây giờ sẽ gọi đến JobDAO
    public List<JobSearchResult> searchJobs(String skills, String location,
                                            Double minSalary, Double maxSalary,
                                            int page, int size) {
        return jobDAO.searchJobs(skills, location, minSalary, maxSalary, page, size);
    }

        public JobPosting getJobById(int jobId) {
            return jobDAO.findById(jobId);
        }

    public List<JobPosting> getRelatedJobs(Integer categoryId, int excludeJobId) {
        if (categoryId == null) {
            return List.of();
        }
        return jobDAO.findRelatedJobs(categoryId, excludeJobId, 3);
    }
}