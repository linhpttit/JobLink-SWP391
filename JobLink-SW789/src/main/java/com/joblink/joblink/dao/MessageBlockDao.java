
package com.joblink.joblink.dao;

import com.joblink.joblink.model.MessageBlock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MessageBlockDao {
    private final JdbcTemplate jdbc;

    public MessageBlockDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isBlocked(int blockerUserId, int blockedUserId) {
        String sql = """
            SELECT COUNT(*) 
            FROM MessageBlocks 
            WHERE blocker_user_id = ? AND blocked_user_id = ?
            """;
        Integer count = jdbc.queryForObject(sql, Integer.class, blockerUserId, blockedUserId);
        return count != null && count > 0;
    }

    public void blockUser(int blockerUserId, int blockedUserId, String reason) {
        String sql = """
            IF NOT EXISTS (SELECT 1 FROM MessageBlocks 
                          WHERE blocker_user_id = ? AND blocked_user_id = ?)
            BEGIN
                INSERT INTO MessageBlocks (blocker_user_id, blocked_user_id, reason, blocked_at)
                VALUES (?, ?, ?, GETDATE())
            END
            """;
        jdbc.update(sql, blockerUserId, blockedUserId, blockerUserId, blockedUserId, reason);
    }

    public void unblockUser(int blockerUserId, int blockedUserId) {
        String sql = """
            DELETE FROM MessageBlocks 
            WHERE blocker_user_id = ? AND blocked_user_id = ?
            """;
        jdbc.update(sql, blockerUserId, blockedUserId);
    }
}
