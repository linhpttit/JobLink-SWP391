package com.joblink.joblink.service;

import com.joblink.joblink.dao.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SubscriptionSeekerService {
    private final SubscriptionDao subscriptionDao;
    private final EmailHistoryDao emailHistoryDao;
    private final JobSkillDao jobSkillDao;

    public SubscriptionSeekerService(SubscriptionDao subscriptionDao,
                                     EmailHistoryDao emailHistoryDao,
                                     JobSkillDao jobSkillDao) {
        this.subscriptionDao = subscriptionDao;
        this.emailHistoryDao = emailHistoryDao;
        this.jobSkillDao = jobSkillDao;
    }

    // Get all subscriptions for a seeker
    public List<Map<String, Object>> getSubscriptionsBySeeker(int seekerId) {
        return subscriptionDao.getSubscriptionsBySeekerId(seekerId);
    }

    // Get all available skills for selection
    public List<Map<String, Object>> getAllAvailableSkills() {
        return subscriptionDao.getAllSkills();
    }

    // Get all provinces for location selection
    public List<Map<String, Object>> getAllProvinces() {
        return subscriptionDao.getAllProvinces();
    }

    // Create a new subscription
    public int createSubscription(int seekerId, int skillId, int provinceId) {
        // Check if subscription already exists
        if (subscriptionDao.subscriptionExists(seekerId, skillId, provinceId)) {
            throw new IllegalArgumentException("Subscription already exists");
        }
        return subscriptionDao.createSubscription(seekerId, skillId, provinceId);
    }

    // Delete a subscription
    public void cancelSubscription(int subscriptionId) {
        subscriptionDao.deleteSubscription(subscriptionId);
    }

    // Get subscription statistics for a seeker
    public Map<String, Object> getSubscriptionStats(int seekerId) {
        Map<String, Object> stats = new HashMap<>();
        int totalSubscriptions = subscriptionDao.getSubscriptionCount(seekerId);
        List<Map<String, Object>> subscriptions = getSubscriptionsBySeeker(seekerId);

        stats.put("totalSubscriptions", totalSubscriptions);
        stats.put("subscriptions", subscriptions);
        stats.put("emailStats", emailHistoryDao.getEmailStatistics(seekerId));

        return stats;
    }
}