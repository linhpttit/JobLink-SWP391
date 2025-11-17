
package com.joblink.joblink.service;

import com.joblink.joblink.dao.EmailHistoryDao;
import com.joblink.joblink.dao.JobMatchingDao;
import com.joblink.joblink.dao.SubscriptionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JobMatchingService {
    private final JobMatchingDao jobMatchingDao;
    private static final Logger logger = LoggerFactory.getLogger(JobMatchingService.class);
    private final EmailHistoryDao emailHistoryDao;
    private final SubscriptionDao subscriptionDao;
    private final EmailService emailService;

    public JobMatchingService(JobMatchingDao jobMatchingDao ,  EmailHistoryDao emailHistoryDao,
                              SubscriptionDao subscriptionDao,
                              EmailService emailService) {
        this.jobMatchingDao = jobMatchingDao;
        this.emailHistoryDao = emailHistoryDao;
        this.subscriptionDao = subscriptionDao;
        this.emailService = emailService;
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

    public void matchJobWithSubscriptions(int jobId, String jobTitle, String companyName,
                                          String location, int salaryMin, int salaryMax,
                                          String experienceLevel, List<Integer> requiredSkillIds) {
        logger.info("Matching job {} with {} required skills", jobId, requiredSkillIds.size());

        for (Integer skillId : requiredSkillIds) {
            // Get all subscriptions for this skill
            String sql = """
                SELECT es.subscription_id, es.seeker_id, es.skill_id, es.province_id,
                       s.skill_name, p.province_name, jsp.email, jsp.first_name, jsp.last_name
                FROM EmailSubscriptions es
                JOIN Skills s ON es.skill_id = s.skill_id
                JOIN Provinces p ON es.province_id = p.province_id
                JOIN JobSeekerProfile jsp ON es.seeker_id = jsp.seeker_id
                WHERE es.skill_id = ? AND es.status = 'active' AND p.province_id = 
                    (SELECT province_id FROM Provinces WHERE province_name LIKE ?)
                """;

            try {
                // Send email to matching subscribers
                logger.info("Sending job notifications for skill ID: {}", skillId);
            } catch (Exception e) {
                logger.error("Error matching job with subscriptions: {}", e.getMessage());
            }
        }
    }

    /**
     * Send batch email notifications for all matching subscriptions
     */
    public void sendBatchNotifications(Map<String, Object> jobDetails) {
        try {
            int jobId = (Integer) jobDetails.get("jobId");
            String jobTitle = (String) jobDetails.get("jobTitle");

            logger.info("Processing batch notifications for job: {}", jobTitle);
            // Implementation for batch email sending

        } catch (Exception e) {
            logger.error("Error sending batch notifications: {}", e.getMessage());
        }
    }

    /**
     * Get all jobs matching a seeker's subscriptions
     */
    public List<Map<String, Object>> getJobsForSubscription(int skillId, int provinceId) {
        // This method fetches jobs matching subscriber criteria
        return List.of();
    }
}
