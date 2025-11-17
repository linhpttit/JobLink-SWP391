package com.joblink.joblink.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class EmailHistoryDao {
    private final JdbcTemplate jdbc;

    public EmailHistoryDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Log sent email
    public void logEmailSent(int seekerId, int jobId, String recipientEmail, String subject) {
        String sql = """
            INSERT INTO EmailHistory (seeker_id, job_id, recipient_email, subject, status)
            VALUES (?, ?, ?, ?, 'sent')
            """;
        jdbc.update(sql, seekerId, jobId, recipientEmail, subject);
    }

    // Log failed email
    public void logEmailFailed(int seekerId, int jobId, String recipientEmail, String subject, String errorMessage) {
        String sql = """
            INSERT INTO EmailHistory (seeker_id, job_id, recipient_email, subject, status, error_message)
            VALUES (?, ?, ?, ?, 'failed', ?)
            """;
        jdbc.update(sql, seekerId, jobId, recipientEmail, subject, errorMessage);
    }

    // Get email history for a seeker
    public List<Map<String, Object>> getEmailHistoryBySeekerId(int seekerId) {
        String sql = """
            SELECT email_id, seeker_id, job_id, recipient_email, subject, sent_at, status
            FROM EmailHistory
            WHERE seeker_id = ?
            ORDER BY sent_at DESC
            """;
        return jdbc.queryForList(sql, seekerId);
    }

    // Get email statistics
    public Map<String, Object> getEmailStatistics(int seekerId) {
        String sql = """
            SELECT 
                COUNT(*) as total_emails,
                SUM(CASE WHEN status = 'sent' THEN 1 ELSE 0 END) as sent_emails,
                SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) as failed_emails
            FROM EmailHistory
            WHERE seeker_id = ?
            """;
        List<Map<String, Object>> result = jdbc.queryForList(sql, seekerId);
        return result.isEmpty() ? new HashMap<>() : result.get(0);
    }
}
