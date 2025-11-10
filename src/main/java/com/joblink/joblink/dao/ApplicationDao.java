package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.Application; // Sửa lại package model nếu cần
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ApplicationDao {

    private final JdbcTemplate jdbcTemplate;

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
}