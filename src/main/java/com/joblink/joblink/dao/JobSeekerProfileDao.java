package com.joblink.joblink.dao;

import com.joblink.joblink.model.JobSeekerProfile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class JobSeekerProfileDao {

    private final JdbcTemplate jdbc;

    public JobSeekerProfileDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ===== READ =====

    /** Tìm hồ sơ theo user_id (dùng cho user đang đăng nhập) */
    public JobSeekerProfile findByUserId(int userId) {
        String sql = """
            SELECT seeker_id, user_id, fullname, gender, location, headline,
                   experience_years, about, email, phone, dob, avatar_url, completion_percentage, updated_at
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
                p.setExperienceYears((Integer) rs.getObject("experience_years"));
                p.setAbout(rs.getString("about"));
                p.setEmail(rs.getString("email"));
                p.setPhoneNumber(rs.getString("phone"));

                Date dob = rs.getDate("dob");
                p.setDateOfBirth(dob != null ? dob.toLocalDate() : null);

                p.setAvatarUrl(rs.getString("avatar_url"));
                p.setCompletionPercentage((Integer) rs.getObject("completion_percentage"));

                return p;
            }, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** Tìm hồ sơ theo seeker_id (dùng khi đã có khóa chính) */
    public JobSeekerProfile findBySeekerId(int seekerId) {
        String sql = """
            SELECT seeker_id, user_id, fullname, gender, location, headline,
                   experience_years, about, email, phone, dob, avatar_url, completion_percentage, updated_at
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
                p.setExperienceYears((Integer) rs.getObject("experience_years"));
                p.setAbout(rs.getString("about"));
                p.setEmail(rs.getString("email"));
                p.setPhoneNumber(rs.getString("phone"));

                Date dob = rs.getDate("dob");
                p.setDateOfBirth(dob != null ? dob.toLocalDate() : null);

                p.setAvatarUrl(rs.getString("avatar_url"));
                p.setCompletionPercentage((Integer) rs.getObject("completion_percentage"));

                return p;
            }, seekerId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** Tìm người có kỹ năng trùng (không gồm bản thân) */
    public List<JobSeekerProfile> findSeekersWithOverlappingSkills(int seekerId) {
        String sql = """
            SELECT DISTINCT jsp.seeker_id, jsp.user_id, jsp.fullname, jsp.gender, jsp.location, 
                   jsp.headline, jsp.experience_years, jsp.about, jsp.email, jsp.phone, 
                   jsp.dob, jsp.avatar_url, jsp.completion_percentage, jsp.updated_at,
                   COUNT(DISTINCT ss2.skill_id) AS shared_skills_count
            FROM JobSeekerProfile jsp
            JOIN SeekerSkills ss2 ON jsp.seeker_id = ss2.seeker_id
            WHERE ss2.skill_id IN (SELECT skill_id FROM SeekerSkills WHERE seeker_id = ?)
              AND jsp.seeker_id <> ?
            GROUP BY jsp.seeker_id, jsp.user_id, jsp.fullname, jsp.gender, jsp.location, 
                     jsp.headline, jsp.experience_years, jsp.about, jsp.email, jsp.phone, 
                     jsp.dob, jsp.avatar_url, jsp.completion_percentage, jsp.updated_at
            ORDER BY shared_skills_count DESC
            """;

        return jdbc.query(sql, (rs, rowNum) -> {
            JobSeekerProfile p = new JobSeekerProfile();
            p.setSeekerId(rs.getInt("seeker_id"));
            p.setUserId(rs.getInt("user_id"));
            p.setFullname(rs.getString("fullname"));
            p.setGender(rs.getString("gender"));
            p.setLocation(rs.getString("location"));
            p.setHeadline(rs.getString("headline"));
            p.setExperienceYears((Integer) rs.getObject("experience_years"));
            p.setAbout(rs.getString("about"));
            p.setEmail(rs.getString("email"));
            p.setPhoneNumber(rs.getString("phone"));

            Date dob = rs.getDate("dob");
            p.setDateOfBirth(dob != null ? dob.toLocalDate() : null);

            p.setAvatarUrl(rs.getString("avatar_url"));
            p.setCompletionPercentage((Integer) rs.getObject("completion_percentage"));

            // Nếu muốn lưu shared_skills_count vào model, thêm field tương ứng trong JobSeekerProfile
            return p;
        }, seekerId, seekerId);
    }

    // ===== CREATE =====

    /** Tạo hồ sơ rỗng cho user (nếu chưa có). Không chèn seeker_id để giữ IDENTITY */
    public int createEmptyProfileByUserId(int userId) {
        String sql = """
            IF NOT EXISTS (SELECT 1 FROM JobSeekerProfile WHERE user_id = ?)
            BEGIN
                INSERT INTO JobSeekerProfile
                (user_id, fullname, gender, location, headline,
                 experience_years, about, email, phone, dob, avatar_url, completion_percentage, updated_at)
                VALUES (?, NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL, NULL, NULL, 0, GETDATE());
            END
            """;
        return jdbc.update(sql, userId, userId);
    }

    // ===== UPDATE =====

    /** Cập nhật hồ sơ theo seeker_id */
    public int updateProfile(JobSeekerProfile profile) {
        String sql = """
            UPDATE JobSeekerProfile
            SET fullname = ?, gender = ?, location = ?, headline = ?,
                experience_years = ?, about = ?, email = ?, phone = ?, dob = ?, avatar_url = ?, 
                completion_percentage = ?, updated_at = GETDATE()
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
