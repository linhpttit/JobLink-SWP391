// File: CVUploadDao.java (PHIÊN BẢN HOÀN CHỈNH)
package com.joblink.joblink.dao;

import com.joblink.joblink.model.CVUpload;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class CVUploadDao {
    private final JdbcTemplate jdbc;

    public CVUploadDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // RowMapper chung để tái sử dụng
    private final RowMapper<CVUpload> cvUploadRowMapper = (rs, rowNum) -> {
        CVUpload cv = new CVUpload();
        cv.setCvId(rs.getInt("cv_id"));
        cv.setSeekerId(rs.getInt("seeker_id"));
        cv.setFullName(rs.getString("full_name"));
        cv.setPhoneNumber(rs.getString("phone_number"));
        cv.setEmail(rs.getString("email"));
        cv.setPreferredLocation(rs.getString("preferred_location"));
        cv.setYearsOfExperience(rs.getObject("years_of_experience", Integer.class));
        cv.setCurrentJobLevel(rs.getString("current_job_level"));
        cv.setWorkMode(rs.getString("work_mode"));
        cv.setExpectedSalary(rs.getString("expected_salary"));
        cv.setCurrentSalary(rs.getString("current_salary"));
        cv.setCoverLetter(rs.getString("cover_letter"));
        cv.setCvFileUrl(rs.getString("cv_file_url"));
        cv.setCvFileName(rs.getString("cv_file_name"));
        Timestamp ts = rs.getTimestamp("uploaded_at");
        if (ts != null) cv.setUploadedAt(ts.toLocalDateTime());
        return cv;
    };

    public int create(CVUpload cv) {
        String sql = """
            INSERT INTO CVUpload 
            (seeker_id, full_name, phone_number, email, preferred_location,
             years_of_experience, current_job_level, work_mode, expected_salary,
             current_salary, cover_letter, cv_file_url, cv_file_name, uploaded_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, cv.getSeekerId());
            ps.setString(2, cv.getFullName());
            ps.setString(3, cv.getPhoneNumber());
            ps.setString(4, cv.getEmail());
            ps.setString(5, cv.getPreferredLocation());
            ps.setObject(6, cv.getYearsOfExperience());
            ps.setString(7, cv.getCurrentJobLevel());
            ps.setString(8, cv.getWorkMode());
            ps.setString(9, cv.getExpectedSalary());
            ps.setString(10, cv.getCurrentSalary());
            ps.setString(11, cv.getCoverLetter());
            ps.setString(12, cv.getCvFileUrl());
            ps.setString(13, cv.getCvFileName());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    // === PHƯƠNG THỨC CÒN THIẾU MÀ BẠN CẦN ===
    /**
     * Lấy tất cả CV của một người tìm việc, sắp xếp theo ngày tải lên gần nhất.
     * @param seekerId ID của người tìm việc.
     * @return Danh sách các CVUpload.
     */
    public List<CVUpload> findBySeekerId(int seekerId) {
        String sql = "SELECT * FROM CVUpload WHERE seeker_id = ? ORDER BY uploaded_at DESC";
        return jdbc.query(sql, cvUploadRowMapper, seekerId);
    }

    public List<CVUpload> findRecentBySeekerId(int seekerId, int limit) {
        String sql = "SELECT TOP (?) * FROM CVUpload WHERE seeker_id = ? ORDER BY uploaded_at DESC";
        return jdbc.query(sql, cvUploadRowMapper, limit, seekerId);
    }

    public CVUpload findById(int cvId) {
        String sql = "SELECT * FROM CVUpload WHERE cv_id = ?";
        try {
            return jdbc.queryForObject(sql, cvUploadRowMapper, cvId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM CVUpload";
        Long count = jdbc.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}