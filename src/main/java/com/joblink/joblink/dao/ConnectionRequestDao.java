
package com.joblink.joblink.dao;

import com.joblink.joblink.model.ConnectionRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ConnectionRequestDao {
    private final JdbcTemplate jdbc;

    public ConnectionRequestDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int create(ConnectionRequest request) {
        String sql = """
            INSERT INTO ConnectionRequests (requester_seeker_id, target_seeker_id, status, message, common_skills)
            VALUES (?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, request.getRequesterSeekerId());
            ps.setInt(2, request.getTargetSeekerId());
            ps.setString(3, request.getStatus());
            ps.setString(4, request.getMessage());
            ps.setString(5, request.getCommonSkills());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public List<ConnectionRequest> findPendingByTargetSeekerId(int targetSeekerId) {
        String sql = """
            SELECT cr.request_id, cr.requester_seeker_id, cr.target_seeker_id, cr.status,
                   cr.message, cr.common_skills, cr.created_at, cr.responded_at,
                   jsp.fullname as requester_name, jsp.avatar_url as requester_avatar, jsp.user_id as requester_user_id
            FROM ConnectionRequests cr
            LEFT JOIN JobSeekerProfile jsp ON cr.requester_seeker_id = jsp.seeker_id
            WHERE cr.target_seeker_id = ? AND cr.status = 'PENDING'
            ORDER BY cr.created_at DESC
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            ConnectionRequest req = new ConnectionRequest();
            req.setRequestId(rs.getInt("request_id"));
            req.setRequesterSeekerId(rs.getInt("requester_seeker_id"));
            req.setTargetSeekerId(rs.getInt("target_seeker_id"));
            req.setStatus(rs.getString("status"));
            req.setMessage(rs.getString("message"));
            req.setCommonSkills(rs.getString("common_skills"));
            if (rs.getTimestamp("created_at") != null) {
                req.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("responded_at") != null) {
                req.setRespondedAt(rs.getTimestamp("responded_at").toLocalDateTime());
            }
            req.setRequesterName(rs.getString("requester_name"));
            req.setRequesterAvatar(rs.getString("requester_avatar"));
            req.setRequesterUserId(rs.getInt("requester_user_id"));
            return req;
        }, targetSeekerId);
    }

    public List<ConnectionRequest> findByRequesterSeekerId(int requesterSeekerId) {
        String sql = """
            SELECT cr.request_id, cr.requester_seeker_id, cr.target_seeker_id, cr.status,
                   cr.message, cr.common_skills, cr.created_at, cr.responded_at,
                   jsp.fullname as target_name, jsp.avatar_url as target_avatar, jsp.user_id as target_user_id
            FROM ConnectionRequests cr
            LEFT JOIN JobSeekerProfile jsp ON cr.target_seeker_id = jsp.seeker_id
            WHERE cr.requester_seeker_id = ?
            ORDER BY cr.created_at DESC
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            ConnectionRequest req = new ConnectionRequest();
            req.setRequestId(rs.getInt("request_id"));
            req.setRequesterSeekerId(rs.getInt("requester_seeker_id"));
            req.setTargetSeekerId(rs.getInt("target_seeker_id"));
            req.setStatus(rs.getString("status"));
            req.setMessage(rs.getString("message"));
            req.setCommonSkills(rs.getString("common_skills"));
            if (rs.getTimestamp("created_at") != null) {
                req.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("responded_at") != null) {
                req.setRespondedAt(rs.getTimestamp("responded_at").toLocalDateTime());
            }
            req.setTargetName(rs.getString("target_name"));
            req.setTargetAvatar(rs.getString("target_avatar"));
            req.setTargetUserId(rs.getInt("target_user_id"));
            return req;
        }, requesterSeekerId);
    }

    public ConnectionRequest findById(int requestId) {
        String sql = """
            SELECT request_id, requester_seeker_id, target_seeker_id, status, message, common_skills, created_at, responded_at
            FROM ConnectionRequests
            WHERE request_id = ?
            """;
        List<ConnectionRequest> results = jdbc.query(sql, (rs, rowNum) -> {
            ConnectionRequest req = new ConnectionRequest();
            req.setRequestId(rs.getInt("request_id"));
            req.setRequesterSeekerId(rs.getInt("requester_seeker_id"));
            req.setTargetSeekerId(rs.getInt("target_seeker_id"));
            req.setStatus(rs.getString("status"));
            req.setMessage(rs.getString("message"));
            req.setCommonSkills(rs.getString("common_skills"));
            if (rs.getTimestamp("created_at") != null) {
                req.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("responded_at") != null) {
                req.setRespondedAt(rs.getTimestamp("responded_at").toLocalDateTime());
            }
            return req;
        }, requestId);
        return results.isEmpty() ? null : results.get(0);
    }

    public ConnectionRequest findExistingRequest(int seekerId1, int seekerId2) {
        String sql = """
            SELECT request_id, requester_seeker_id, target_seeker_id, status, message, common_skills, created_at, responded_at
            FROM ConnectionRequests
            WHERE ((requester_seeker_id = ? AND target_seeker_id = ?) OR (requester_seeker_id = ? AND target_seeker_id = ?))
              AND status IN ('PENDING', 'ACCEPTED')
            """;
        List<ConnectionRequest> results = jdbc.query(sql, (rs, rowNum) -> {
            ConnectionRequest req = new ConnectionRequest();
            req.setRequestId(rs.getInt("request_id"));
            req.setRequesterSeekerId(rs.getInt("requester_seeker_id"));
            req.setTargetSeekerId(rs.getInt("target_seeker_id"));
            req.setStatus(rs.getString("status"));
            req.setMessage(rs.getString("message"));
            req.setCommonSkills(rs.getString("common_skills"));
            if (rs.getTimestamp("created_at") != null) {
                req.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("responded_at") != null) {
                req.setRespondedAt(rs.getTimestamp("responded_at").toLocalDateTime());
            }
            return req;
        }, seekerId1, seekerId2, seekerId2, seekerId1);
        return results.isEmpty() ? null : results.get(0);
    }

    public void updateStatus(int requestId, String status) {
        String sql = "UPDATE ConnectionRequests SET status = ?, responded_at = GETDATE() WHERE request_id = ?";
        jdbc.update(sql, status, requestId);
    }

    public List<Integer> findSeekersWithCommonSkills(int seekerId) {
        String sql = """
            SELECT DISTINCT ss2.seeker_id
            FROM SeekerSkills ss1
            INNER JOIN SeekerSkills ss2 ON ss1.skill_name = ss2.skill_name
            WHERE ss1.seeker_id = ? AND ss2.seeker_id != ?
            """;
        return jdbc.queryForList(sql, Integer.class, seekerId, seekerId);
    }

    public List<String> findCommonSkills(int seekerId1, int seekerId2) {
        String sql = """
            SELECT DISTINCT ss1.skill_name
            FROM SeekerSkills ss1
            INNER JOIN SeekerSkills ss2 ON ss1.skill_name = ss2.skill_name
            WHERE ss1.seeker_id = ? AND ss2.seeker_id = ?
            """;
        return jdbc.queryForList(sql, String.class, seekerId1, seekerId2);
    }
}
