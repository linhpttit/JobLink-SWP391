package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.JobSeekerProfile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JobSeekerProfileDao {

    private final JdbcTemplate jdbc;

    public JobSeekerProfileDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Lấy hồ sơ theo userId từ bảng dbo.Users (vì hiện tại chưa có bảng JobSeekerProfile riêng).
     * Trả về JobSeekerProfile "lightweight" – những trường khác để null.
     */
    public JobSeekerProfile findByUserId(int userId) {
        final String sql = """
            SELECT user_id, email, username, url_avt
            FROM dbo.Users
            WHERE user_id = ?
            """;
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> {
                JobSeekerProfile p = new JobSeekerProfile();
                p.setUserId(rs.getInt("user_id"));
                p.setEmail(rs.getString("email"));

                // Ứng tạm: fullName = username (vì bảng Users hiện chưa có cột full_name)
                String username = rs.getString("username");
                p.setUsername(username);
                p.setFullName(username);

                // avatar nếu có
                p.setAvatarUrl(rs.getString("url_avt"));

                // Các trường khác (about/phone/address/experienceYears/skills/experiences) giữ null
                return p;
            }, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


}
