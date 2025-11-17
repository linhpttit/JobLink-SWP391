package com.joblink.joblink.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DashboardDao {
    private final JdbcTemplate jdbc;

    public DashboardDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    public Map<String, Object> getUserProfileInfo(int seekerId) {
        String sql = """
            SELECT 
                jsp.seeker_id,
                jsp.fullname,
                jsp.headline,
                jsp.email,
                jsp.phone,
                jsp.location,
                jsp.avatar_url,
                jsp.completion_percentage,
                u.email as user_email
            FROM JobSeekerProfile jsp
            JOIN Users u ON jsp.user_id = u.user_id
            WHERE jsp.seeker_id = ?
            """;

        List<Map<String, Object>> results = jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> profile = new HashMap<>();
            profile.put("seekerId", rs.getInt("seeker_id"));
            profile.put("fullname", rs.getString("fullname"));
            profile.put("headline", rs.getString("headline"));
            profile.put("email", rs.getString("email") != null ? rs.getString("email") : rs.getString("user_email"));
            profile.put("phone", rs.getString("phone"));
            profile.put("location", rs.getString("location"));
            profile.put("avatarUrl", rs.getString("avatar_url"));
            profile.put("completionPercentage", rs.getInt("completion_percentage"));
            return profile;
        }, seekerId);

        return results.isEmpty() ? new HashMap<>() : results.get(0);
    }

    public Map<String, Object> getMostRecentCV(int seekerId) {
        String sql = """
            SELECT TOP 1
                cv_id,
                cv_file_name,
                uploaded_at,
                current_job_level,
                years_of_experience
            FROM CVUpload
            WHERE seeker_id = ?
            ORDER BY uploaded_at DESC
            """;

        List<Map<String, Object>> results = jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> cv = new HashMap<>();
            cv.put("cvId", rs.getInt("cv_id"));
            cv.put("fileName", rs.getString("cv_file_name"));
            cv.put("uploadedAt", rs.getTimestamp("uploaded_at"));
            cv.put("jobLevel", rs.getString("current_job_level"));
            cv.put("yearsOfExperience", rs.getInt("years_of_experience"));
            return cv;
        }, seekerId);

        return results.isEmpty() ? null : results.get(0);
    }

    public Map<String, Object> getStatistics(int seekerId) {
        Map<String, Object> stats = new HashMap<>();

        // Total applications
        String applicationsSql = "SELECT COUNT(*) as total FROM Applications WHERE seeker_id = ?";
        Integer totalApplications = jdbc.queryForObject(applicationsSql, Integer.class, seekerId);
        stats.put("totalApplications", totalApplications != null ? totalApplications : 0);

        // Companies followed
        String followsSql = "SELECT COUNT(*) as total FROM CompanyFollows WHERE seeker_id = ?";
        Integer totalFollows = jdbc.queryForObject(followsSql, Integer.class, seekerId);
        stats.put("companiesFollowed", totalFollows != null ? totalFollows : 0);

        // Total spending (from invoices)
        String spendingSql = """
            SELECT COALESCE(SUM(i.amount), 0) as total_spending
            FROM Invoice i
            WHERE i.seeker_id = ? AND i.status = 'PAID'
            """;
        Double totalSpending = jdbc.queryForObject(spendingSql, Double.class, seekerId);
        stats.put("totalSpending", totalSpending != null ? totalSpending : 0.0);

        return stats;
    }
}
