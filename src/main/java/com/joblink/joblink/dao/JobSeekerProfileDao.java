package com.joblink.joblink.dao;

import com.joblink.joblink.model.JobSeekerProfile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JobSeekerProfileDao {
    private final JdbcTemplate jdbc;

    public JobSeekerProfileDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ===== READ =====

    // Tìm theo user_id (ổn nhất khi seeker_id là IDENTITY)
    public JobSeekerProfile findByUserId(int userId) {
        String sql = """
            SELECT seeker_id, user_id, fullname, gender, location, headline,
                   experience_years, about, email, phone, dob, avatar_url, completion_percentage
            FROM JobSeekerProfile
            WHERE user_id = ?
            """;
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> {
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
                p.setDateOfBirth(rs.getObject("dob", java.time.LocalDate.class));
                p.setAvatarUrl(rs.getString("avatar_url"));
                p.setCompletionPercentage(rs.getObject("completion_percentage", Integer.class));
                return p;
            }, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Vẫn giữ nếu cần tìm trực tiếp bằng seeker_id
    public JobSeekerProfile findBySeekerId(int seekerId) {
        String sql = """
            SELECT seeker_id, user_id, fullname, gender, location, headline,
                   experience_years, about, email, phone, dob, avatar_url, completion_percentage
            FROM JobSeekerProfile
            WHERE seeker_id = ?
            """;
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> {
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
                p.setDateOfBirth(rs.getObject("dob", java.time.LocalDate.class));
                p.setAvatarUrl(rs.getString("avatar_url"));
                p.setCompletionPercentage(rs.getObject("completion_percentage", Integer.class));
                return p;
            }, seekerId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ===== CREATE =====

    // Tạo hồ sơ rỗng theo user_id (KHÔNG chèn seeker_id để tránh lỗi IDENTITY)
    public int createEmptyProfileByUserId(int userId) {
        String sql = """
            IF NOT EXISTS (SELECT 1 FROM JobSeekerProfile WHERE user_id = ?)
            BEGIN
                INSERT INTO JobSeekerProfile
                (user_id, fullname, gender, location, headline,
                 experience_years, about, email, phone, dob, avatar_url, completion_percentage)
                VALUES (?, NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL, NULL, NULL, 0);
            END
            """;
        return jdbc.update(sql, userId, userId);
    }

    // ===== UPDATE =====

    // Cập nhật theo seeker_id (khi đã có profile)
    public int updateProfile(JobSeekerProfile profile) {
        String sql = """
            UPDATE JobSeekerProfile
            SET fullname = ?, gender = ?, location = ?, headline = ?,
                experience_years = ?, about = ?, email = ?, phone = ?, dob = ?, avatar_url = ?, 
                completion_percentage = ?
            WHERE seeker_id = ?
            """;
        return jdbc.update(sql,
                profile.getFullname(),
                profile.getGender(),
                profile.getLocation(),
                profile.getHeadline(),
                profile.getExperienceYears(),
                profile.getAbout(),
                profile.getEmail(),
                profile.getPhoneNumber(),
                profile.getDateOfBirth(),
                profile.getAvatarUrl(),
                profile.getCompletionPercentage(),
                profile.getSeekerId()
        );
    }

    public int updateCompletionPercentage(int seekerId, int percentage) {
        String sql = "UPDATE JobSeekerProfile SET completion_percentage = ?, updated_at = GETDATE() WHERE seeker_id = ?";
        return jdbc.update(sql, percentage, seekerId);
    }
}
