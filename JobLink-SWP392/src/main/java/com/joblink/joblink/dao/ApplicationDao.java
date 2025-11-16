package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.Application; // Sửa lại package model nếu cần
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository

    public class ApplicationDao {

    private final JdbcTemplate jdbcTemplate;
    public ApplicationDao(JdbcTemplate jdbc) {
        this.jdbcTemplate = jdbc;
    }
    // --- ROW MAPPER ĐÃ BỔ SUNG ---
    private final RowMapper<Application> applicationRowMapper = (rs, rowNum) -> {
        Application app = new Application();
        app.setApplicationId(rs.getInt("application_id"));
        app.setJobId(rs.getInt("job_id"));
        app.setSeekerId(rs.getInt("seeker_id"));
        app.setStatus(rs.getString("status"));
        if (rs.getTimestamp("applied_at") != null) {
            app.setAppliedAt(rs.getTimestamp("applied_at").toLocalDateTime());
        }
        if (rs.getTimestamp("last_status_at") != null) {
            app.setLastStatusAt(rs.getTimestamp("last_status_at").toLocalDateTime());
        }
        app.setCvUrl(rs.getString("cv_url"));
        app.setNote(rs.getString("note"));
        app.setStatusLog(rs.getString("status_log"));
        return app;
    };

    public void create(Application application) {
        String sql = "EXEC dbo.sp_Application_Create ?, ?, ?, ?";
        jdbcTemplate.update(sql,
                application.getJobId(),
                application.getSeekerId(),
                application.getCvUrl(),
                application.getNote()
        );
    }

    // --- PHƯƠƠNG THỨC ĐÃ HOÀN THIỆN ---
    public List<Application> findBySeekerId(int seekerId) {
        // Cần JOIN với JobsPosting và EmployerProfile để lấy thêm thông tin hiển thị
        String sql = """
            SELECT 
                a.*, 
                j.title AS job_title, 
                e.company_name 
            FROM Applications a
            JOIN JobsPosting j ON a.job_id = j.job_id
            JOIN EmployerProfile e ON j.employer_id = e.employer_id
            WHERE a.seeker_id = ?
            ORDER BY a.applied_at DESC
            """;
        // Cần tạo một RowMapper khác hoặc mở rộng model Application để chứa job_title, company_name
        // Tạm thời dùng RowMapper cũ, bạn cần mở rộng sau
        return jdbcTemplate.query(sql, applicationRowMapper, seekerId);
    }
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

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
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