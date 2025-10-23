
package com.joblink.joblink.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ApplicationDao {
    private final JdbcTemplate jdbc;

    public ApplicationDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Get companies that a seeker has applied to
     */
    public List<Map<String, Object>> findAppliedCompaniesBySeeker(int seekerId) {
        String sql = """
            SELECT DISTINCT
                e.employer_id,
                e.company_name,
                e.industry,
                e.location,
                e.description,
                u.user_id as employer_user_id,
                COUNT(DISTINCT a.application_id) as total_applications,
                MAX(a.applied_at) as last_applied_at
            FROM Applications a
            INNER JOIN JobsPosting j ON a.job_id = j.job_id
            INNER JOIN EmployerProfile e ON j.employer_id = e.employer_id
            INNER JOIN Users u ON e.user_id = u.user_id
            WHERE a.seeker_id = ?
            GROUP BY e.employer_id, e.company_name, e.industry, e.location, e.description, u.user_id
            ORDER BY last_applied_at DESC
            """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> company = new HashMap<>();
            company.put("employerId", rs.getInt("employer_id"));
            company.put("companyName", rs.getString("company_name"));
            company.put("industry", rs.getString("industry"));
            company.put("location", rs.getString("location"));
            company.put("description", rs.getString("description"));
            company.put("employerUserId", rs.getInt("employer_user_id"));
            company.put("totalApplications", rs.getInt("total_applications"));
            company.put("lastAppliedAt", rs.getTimestamp("last_applied_at"));
            return company;
        }, seekerId);
    }
}
