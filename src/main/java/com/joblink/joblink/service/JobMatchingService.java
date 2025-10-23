
package com.joblink.joblink.service;

import com.joblink.joblink.dao.JobMatchingDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JobMatchingService {
    private final JobMatchingDao jobMatchingDao;

    public JobMatchingService(JobMatchingDao jobMatchingDao) {
        this.jobMatchingDao = jobMatchingDao;
    }

    /**
     * Get top N matching jobs for a seeker based on skill overlap
     */
    public List<Map<String, Object>> getTopMatchingJobs(int seekerId, int limit) {
        return jobMatchingDao.findTopMatchingJobs(seekerId, limit);
    }

    /**
     * Calculate match percentage between seeker skills and job requirements
     */
    public double calculateMatchPercentage(int seekerId, int jobId) {
        return jobMatchingDao.calculateMatchPercentage(seekerId, jobId);
    }
}
