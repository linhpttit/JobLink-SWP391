// File: JobSeekerProfileDao.java (PHIÊN BẢN SỬA LỖI HOÀN CHỈNH)
package com.joblink.joblink.dao;


import com.joblink.joblink.model.Conversation;
import com.joblink.joblink.model.JobSeekerProfile2;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JobSeekerProfileDao {

    private final JdbcTemplate jdbcTemplate;
    public List<Conversation> findConversationsByUserId(int userId) {
        String sql = """
                SELECT\s
                                           c.conversation_id,
                                           c.seeker_id,
                                           c.employer_id,
                                           c.seeker_id_2,
                                           c.conversation_type,
                                           c.last_message_at,
                                           c.created_at,
                
                                           -- Tên hiển thị của "đối phương"
                                           CASE\s
                                               WHEN c.conversation_type = 'SEEKER_SEEKER' THEN
                                                   CASE
                                                       WHEN jsp.user_id = ? THEN COALESCE(jsp2.fullname, u_js2.email)
                                                       ELSE COALESCE(jsp.fullname, u_js.email)
                                                   END
                                               WHEN jsp.user_id = ? THEN COALESCE(ep.company_name, u_ep.email)
                                               ELSE COALESCE(jsp.fullname, u_js.email)
                                           END AS other_user_name,
                
                                           -- Ảnh đại diện của "đối phương"
                                           CASE\s
                                               WHEN c.conversation_type = 'SEEKER_SEEKER' THEN
                                                   CASE
                                                       WHEN jsp.user_id = ? THEN COALESCE(jsp2.avatar_url, '/images/user.png')
                                                       ELSE COALESCE(jsp.avatar_url, '/images/user.png')
                                                   END
                                               WHEN jsp.user_id = ? THEN '/images/user.png'
                                               ELSE COALESCE(jsp.avatar_url, '/images/user.png')
                                           END AS other_user_avatar,
                
                                           -- user_id của "đối phương"
                                           CASE\s
                                               WHEN c.conversation_type = 'SEEKER_SEEKER' THEN
                                                   CASE
                                                       WHEN jsp.user_id = ? THEN jsp2.user_id
                                                       ELSE jsp.user_id
                                                   END
                                               WHEN jsp.user_id = ? THEN ep.user_id
                                               ELSE jsp.user_id
                                           END AS other_user_id,
                
                                           -- Tin nhắn cuối cùng
                                           (SELECT TOP 1 m.message_content
                                            FROM Messages m
                                            WHERE m.conversation_id = c.conversation_id
                                            ORDER BY m.sent_at DESC) AS last_message_content,
                
                                           -- Số tin chưa đọc của current user trong hội thoại này
                                           (SELECT COUNT(*)
                                            FROM Messages m2
                                            WHERE m2.conversation_id = c.conversation_id
                                              AND m2.receiver_user_id = ?
                                              AND m2.is_read = 0) AS unread_count,
                
                                           -- Trạng thái bị block bởi current user với "đối phương"
                                           CASE WHEN EXISTS (
                                               SELECT 1 FROM MessageBlocks mb
                                               WHERE mb.blocker_user_id = ?
                                                 AND mb.blocked_user_id = CASE\s
                                                       WHEN c.conversation_type = 'SEEKER_SEEKER' THEN
                                                           CASE
                                                               WHEN jsp.user_id = ? THEN jsp2.user_id
                                                               ELSE jsp.user_id
                                                           END
                                                       WHEN jsp.user_id = ? THEN ep.user_id\s
                                                       ELSE jsp.user_id\s
                                                   END
                                           ) THEN 1 ELSE 0 END AS is_blocked
                
                                       FROM Conversations c
                                       LEFT JOIN JobSeekerProfile jsp ON jsp.seeker_id = c.seeker_id
                                       LEFT JOIN JobSeekerProfile jsp2 ON jsp2.seeker_id = c.seeker_id_2
                                       LEFT JOIN EmployerProfile  ep  ON ep.employer_id  = c.employer_id
                                       LEFT JOIN Users u_js ON u_js.user_id = jsp.user_id
                                       LEFT JOIN Users u_js2 ON u_js2.user_id = jsp2.user_id
                                       LEFT JOIN Users u_ep ON u_ep.user_id = ep.user_id
                
                                       WHERE (jsp.user_id = ? OR ep.user_id = ? OR jsp2.user_id = ?)
                                       ORDER BY c.last_message_at DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
                    Conversation conv = new Conversation();
                    conv.setConversationId(rs.getInt("conversation_id"));
                    conv.setSeekerId(rs.getInt("seeker_id"));
                    conv.setEmployerId(rs.getInt("employer_id"));
                    conv.setSeekerId2(rs.getInt("seeker_id_2"));
                    conv.setConversationType(rs.getString("conversation_type"));

                    if (rs.getTimestamp("last_message_at") != null) {
                        conv.setLastMessageAt(rs.getTimestamp("last_message_at").toLocalDateTime());
                    }
                    if (rs.getTimestamp("created_at") != null) {
                        conv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }

                    conv.setOtherUserName(rs.getString("other_user_name"));
                    conv.setOtherUserAvatar(rs.getString("other_user_avatar"));
                    conv.setOtherUserId(rs.getInt("other_user_id"));
                    conv.setLastMessageContent(rs.getString("last_message_content"));
                    conv.setUnreadCount(rs.getInt("unread_count"));
                    conv.setIsBlocked(rs.getBoolean("is_blocked"));
                    return conv;
                },
                userId, userId, userId, userId, userId, userId, // 1-6
                userId, // 7 unread_count
                userId, userId, userId, // 8-10 is_blocked
                userId, userId, userId  // 11-13 WHERE clause
        );
    }
    public List<JobSeekerProfile2> findSeekersWithOverlappingSkills(int seekerId) {
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

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            JobSeekerProfile2 p = new JobSeekerProfile2();
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
    public Conversation findByParticipants(int seekerId, int employerId) {
        String sql = """
                SELECT conversation_id, seeker_id, employer_id, seeker_id_2, conversation_type, last_message_at, created_at
                FROM Conversations
                WHERE seeker_id = ? AND employer_id = ? AND conversation_type = 'SEEKER_EMPLOYER'
                """;
        List<Conversation> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Conversation conv = new Conversation();
            conv.setConversationId(rs.getInt("conversation_id"));
            conv.setSeekerId(rs.getInt("seeker_id"));
            conv.setEmployerId(rs.getInt("employer_id"));
            conv.setSeekerId2(rs.getInt("seeker_id_2"));
            conv.setConversationType(rs.getString("conversation_type"));
            if (rs.getTimestamp("last_message_at") != null) {
                conv.setLastMessageAt(rs.getTimestamp("last_message_at").toLocalDateTime());
            }
            if (rs.getTimestamp("created_at") != null) {
                conv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return conv;
        }, seekerId, employerId);
        return results.isEmpty() ? null : results.get(0);
    }

    public Conversation findBySeekerPair(int seekerId1, int seekerId2) {
        String sql = """
                SELECT conversation_id, seeker_id, employer_id, seeker_id_2, conversation_type, last_message_at, created_at
                FROM Conversations
                WHERE conversation_type = 'SEEKER_SEEKER'
                  AND ((seeker_id = ? AND seeker_id_2 = ?) OR (seeker_id = ? AND seeker_id_2 = ?))
                """;
        List<Conversation> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Conversation conv = new Conversation();
            conv.setConversationId(rs.getInt("conversation_id"));
            conv.setSeekerId(rs.getInt("seeker_id"));
            conv.setEmployerId(rs.getInt("employer_id"));
            conv.setSeekerId2(rs.getInt("seeker_id_2"));
            conv.setConversationType(rs.getString("conversation_type"));
            if (rs.getTimestamp("last_message_at") != null) {
                conv.setLastMessageAt(rs.getTimestamp("last_message_at").toLocalDateTime());
            }
            if (rs.getTimestamp("created_at") != null) {
                conv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return conv;
        }, seekerId1, seekerId2, seekerId2, seekerId1);
        return results.isEmpty() ? null : results.get(0);
    }

    public Conversation findById(int conversationId) {
        String sql = """
                SELECT conversation_id, seeker_id, employer_id, seeker_id_2, conversation_type, last_message_at, created_at
                FROM Conversations
                WHERE conversation_id = ?
                """;
        List<Conversation> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Conversation conv = new Conversation();
            conv.setConversationId(rs.getInt("conversation_id"));
            conv.setSeekerId(rs.getInt("seeker_id"));
            conv.setEmployerId(rs.getInt("employer_id"));
            conv.setSeekerId2(rs.getInt("seeker_id_2"));
            conv.setConversationType(rs.getString("conversation_type"));
            if (rs.getTimestamp("last_message_at") != null) {
                conv.setLastMessageAt(rs.getTimestamp("last_message_at").toLocalDateTime());
            }
            if (rs.getTimestamp("created_at") != null) {
                conv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return conv;
        }, conversationId);
        return results.isEmpty() ? null : results.get(0);
    }

    public int create(Conversation conversation) {
        if ("SEEKER_SEEKER".equals(conversation.getConversationType())) {
            String sql = """
                INSERT INTO Conversations (seeker_id, seeker_id_2, conversation_type, created_at, last_message_at)
                VALUES (?, ?, 'SEEKER_SEEKER', GETDATE(), GETDATE())
                """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, conversation.getSeekerId());
                ps.setInt(2, conversation.getSeekerId2());
                return ps;
            }, keyHolder);
            return keyHolder.getKey().intValue();
        } else {
            String sql = """
                INSERT INTO Conversations (seeker_id, employer_id, conversation_type, created_at, last_message_at)
                VALUES (?, ?, 'SEEKER_EMPLOYER', GETDATE(), GETDATE())
                """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, conversation.getSeekerId());
                ps.setInt(2, conversation.getEmployerId());
                return ps;
            }, keyHolder);
            return keyHolder.getKey().intValue();
        }
    }
    private final RowMapper<JobSeekerProfile2> profileRowMapper = (rs, rowNum) -> {
        JobSeekerProfile2 p = new JobSeekerProfile2();
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

    public JobSeekerProfile2 findByUserId(int userId) {
        try {
            // SỬA LỖI Ở ĐÂY: Dùng "SELECT *" để lấy tất cả các cột
            String sql = "SELECT * FROM JobSeekerProfile WHERE user_id = ?";
            return jdbcTemplate.queryForObject(sql, profileRowMapper, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public JobSeekerProfile2 findBySeekerId(int seekerId) {
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
    public void updateProfile(JobSeekerProfile2 profile) {
        String sql = """
        UPDATE JobSeekerProfile SET
            fullname = ?, gender = ?, location = ?, headline = ?,
            experience_years = ?, about = ?, email = ?, phone = ?, dob = ?,
            avatar_url = ?  -- 1. THÊM CỘT MỚI VÀO ĐÂY
        WHERE seeker_id = ?
        """;

        // 2. THÊM GETTER TƯƠNG ỨNG VÀO ĐÂY (TRƯỚC SEEKERID)
        jdbcTemplate.update(sql,
                profile.getFullname(), profile.getGender(), profile.getLocation(), profile.getHeadline(),
                profile.getExperienceYears(), profile.getAbout(), profile.getEmail(), profile.getPhoneNumber(),
                profile.getDateOfBirth(),
                profile.getAvatarUrl(),  // <-- Thêm vào đây
                profile.getSeekerId()
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