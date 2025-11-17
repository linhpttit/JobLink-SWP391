package com.joblink.joblink.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class SubscriptionDao {
    private final JdbcTemplate jdbc;

    public SubscriptionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Get all subscriptions for a seeker
    public List<Map<String, Object>> getSubscriptionsBySeekerId(int seekerId) {
        String sql = """
            SELECT es.subscription_id, es.seeker_id, es.skill_id, es.province_id,
                   s.name as skill_name, p.province_name, es.created_at
            FROM EmailSubscriptions es
            JOIN Skills s ON es.skill_id = s.skill_id
            JOIN Provinces p ON es.province_id = p.province_id
            WHERE es.seeker_id = ? AND es.status = 'active'
            ORDER BY es.created_at DESC
            """;
        return jdbc.queryForList(sql, seekerId);
    }

    // Get all skills
    public List<Map<String, Object>> getAllSkills() {
        String sql = "SELECT skill_id, name as skill_name FROM Skills ORDER BY name";
        return jdbc.queryForList(sql);
    }

    // Get all provinces
    public List<Map<String, Object>> getAllProvinces() {
        String sql = "SELECT province_id, province_name FROM Provinces ORDER BY province_name";
        return jdbc.queryForList(sql);
    }

    // Create subscription
    public int createSubscription(int seekerId, int skillId, int provinceId) {
        String sql = """
            INSERT INTO EmailSubscriptions (seeker_id, skill_id, province_id, status)
            VALUES (?, ?, ?, 'active')
            """;
        jdbc.update(sql, seekerId, skillId, provinceId);

        // Get the inserted subscription ID
        String selectSql = """
            SELECT subscription_id FROM EmailSubscriptions 
            WHERE seeker_id = ? AND skill_id = ? AND province_id = ?
            ORDER BY created_at DESC
            """;
        return jdbc.queryForObject(selectSql, Integer.class, seekerId, skillId, provinceId);
    }

    // Delete subscription
    public void deleteSubscription(int subscriptionId) {
        String sql = "DELETE FROM EmailSubscriptions WHERE subscription_id = ?";
        jdbc.update(sql, subscriptionId);
    }

    // Check if subscription already exists
    public boolean subscriptionExists(int seekerId, int skillId, int provinceId) {
        String sql = """
            SELECT COUNT(*) FROM EmailSubscriptions 
            WHERE seeker_id = ? AND skill_id = ? AND province_id = ? AND status = 'active'
            """;
        Integer count = jdbc.queryForObject(sql, Integer.class, seekerId, skillId, provinceId);
        return count != null && count > 0;
    }

    // Get subscription count for seeker
    public int getSubscriptionCount(int seekerId) {
        String sql = "SELECT COUNT(*) FROM EmailSubscriptions WHERE seeker_id = ? AND status = 'active'";
        Integer count = jdbc.queryForObject(sql, Integer.class, seekerId);
        return count != null ? count : 0;
    }
}