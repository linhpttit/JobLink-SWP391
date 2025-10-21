// File: JobSeekerProfileDao.java (PHIÊN BẢN SỬA LỖI HOÀN CHỈNH)
package com.joblink.joblink.dao;

import com.joblink.joblink.model.JobSeekerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JobSeekerProfileDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<JobSeekerProfile> profileRowMapper = (rs, rowNum) -> {
        JobSeekerProfile p = new JobSeekerProfile();
        p.setSeekerId(rs.getInt("seeker_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setFullname(rs.getString("fullname"));
        p.setGender(rs.getString("gender"));
        p.setLocation(rs.getString("location"));
        p.setHeadline(rs.getString("headline"));
        p.setExperienceYears(rs.getObject("experience_years", Integer.class));
        p.setAbout(rs.getString("about"));
        p.setEmail(rs.getString("email"));
        p.setPhoneNumber(rs.getString("phone"));
        p.setDateOfBirth(rs.getDate("dob") != null ? rs.getDate("dob").toLocalDate() : null);
        p.setAvatarUrl(rs.getString("avatar_url"));
        p.setCompletionPercentage(rs.getInt("completion_percentage"));
        // Dòng này bây giờ sẽ hoạt động chính xác
        p.setReceiveInvitations(rs.getBoolean("receive_invitations"));
        return p;
    };

    public JobSeekerProfile findByUserId(int userId) {
        try {
            // SỬA LỖI Ở ĐÂY: Dùng "SELECT *" để lấy tất cả các cột
            String sql = "SELECT * FROM JobSeekerProfile WHERE user_id = ?";
            return jdbcTemplate.queryForObject(sql, profileRowMapper, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public JobSeekerProfile findBySeekerId(int seekerId) {
        try {
            // SỬA LỖI Ở ĐÂY: Dùng "SELECT *" để lấy tất cả các cột
            String sql = "SELECT * FROM JobSeekerProfile WHERE seeker_id = ?";
            return jdbcTemplate.queryForObject(sql, profileRowMapper, seekerId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void createEmptyProfileByUserId(int userId) {
        String sql = "INSERT INTO JobSeekerProfile (user_id) VALUES (?)";
        jdbcTemplate.update(sql, userId);
    }

    public void updateProfile(JobSeekerProfile profile) {
        String sql = """
            UPDATE JobSeekerProfile SET
                fullname = ?, gender = ?, location = ?, headline = ?,
                experience_years = ?, about = ?, email = ?, phone = ?, dob = ?
            WHERE seeker_id = ?
            """;
        jdbcTemplate.update(sql,
                profile.getFullname(), profile.getGender(), profile.getLocation(), profile.getHeadline(),
                profile.getExperienceYears(), profile.getAbout(), profile.getEmail(), profile.getPhoneNumber(),
                profile.getDateOfBirth(), profile.getSeekerId()
        );
    }

    public void updateCompletionPercentage(int seekerId, int percentage) {
        String sql = "UPDATE JobSeekerProfile SET completion_percentage = ? WHERE seeker_id = ?";
        jdbcTemplate.update(sql, percentage, seekerId);
    }

    public int updateReceiveInvitations(int seekerId, boolean enabled) {
        String sql = "UPDATE JobSeekerProfile SET receive_invitations = ? WHERE seeker_id = ?";
        return jdbcTemplate.update(sql, enabled, seekerId);
    }
}