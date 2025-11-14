
package com.joblink.joblink.dao;

import com.joblink.joblink.model.Message;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class MessageDao {
    private final JdbcTemplate jdbc;

    public MessageDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Message> findByConversationId(int conversationId) {
        String sql = """
            SELECT m.message_id, m.conversation_id, m.sender_user_id, m.receiver_user_id,
                   m.message_content, m.message_type, m.is_read, m.is_recalled, 
                   m.recalled_at, m.sent_at,
                   COALESCE(jsp.fullname, ep.company_name, u.email) as sender_name,
                   COALESCE(jsp.avatar_url, '/images/user.png') as sender_avatar
            FROM Messages m
            JOIN Users u ON m.sender_user_id = u.user_id
            LEFT JOIN JobSeekerProfile jsp ON u.user_id = jsp.user_id
            LEFT JOIN EmployerProfile ep ON u.user_id = ep.user_id
            WHERE m.conversation_id = ?
            ORDER BY m.sent_at ASC
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Message msg = new Message();
            msg.setMessageId(rs.getInt("message_id"));
            msg.setConversationId(rs.getInt("conversation_id"));
            msg.setUserId(rs.getInt("sender_user_id"));
            msg.setUserId2(rs.getInt("receiver_user_id"));
            msg.setMessageContent(rs.getString("message_content"));
            msg.setMessageType(rs.getString("message_type"));
            msg.setIsRead(rs.getBoolean("is_read"));
            msg.setIsRecalled(rs.getBoolean("is_recalled"));
            if (rs.getTimestamp("recalled_at") != null) {
                msg.setRecalledAt(rs.getTimestamp("recalled_at").toLocalDateTime());
            }
            if (rs.getTimestamp("sent_at") != null) {
                msg.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
            }
            msg.setSenderName(rs.getString("sender_name"));
            msg.setSenderAvatar(rs.getString("sender_avatar"));
            return msg;
        }, conversationId);
    }

    public int create(Message message) {
        String sql = """
            INSERT INTO Messages (conversation_id, sender_user_id, receiver_user_id, 
                                 message_content, message_type, sent_at)
            VALUES (?, ?, ?, ?, ?, GETDATE())
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, message.getConversationId());
            ps.setInt(2, message.getUserId());
            ps.setInt(3, message.getUserId2());
            ps.setString(4, message.getMessageContent());
            ps.setString(5, message.getMessageType() != null ? message.getMessageType() : "text");
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void markAsRead(int conversationId, int userId) {
        String sql = """
            UPDATE Messages 
            SET is_read = 1 
            WHERE conversation_id = ? AND receiver_user_id = ? AND is_read = 0
            """;
        jdbc.update(sql, conversationId, userId);
    }

    public void recallMessage(int messageId, int userId) {
        String sql = """
            UPDATE Messages 
            SET is_recalled = 1, recalled_at = GETDATE()
            WHERE message_id = ? AND sender_user_id = ? AND is_recalled = 0
            """;
        jdbc.update(sql, messageId, userId);
    }

    public Message findById(int messageId) {
        String sql = """
            SELECT message_id, conversation_id, sender_user_id, receiver_user_id,
                   message_content, message_type, is_read, is_recalled, 
                   recalled_at, sent_at
            FROM Messages
            WHERE message_id = ?
            """;
        List<Message> results = jdbc.query(sql, (rs, rowNum) -> {
            Message msg = new Message();
            msg.setMessageId(rs.getInt("message_id"));
            msg.setConversationId(rs.getInt("conversation_id"));
            msg.setUserId(rs.getInt("sender_user_id"));
            msg.setUserId2(rs.getInt("receiver_user_id"));
            msg.setMessageContent(rs.getString("message_content"));
            msg.setMessageType(rs.getString("message_type"));
            msg.setIsRead(rs.getBoolean("is_read"));
            msg.setIsRecalled(rs.getBoolean("is_recalled"));
            if (rs.getTimestamp("recalled_at") != null) {
                msg.setRecalledAt(rs.getTimestamp("recalled_at").toLocalDateTime());
            }
            if (rs.getTimestamp("sent_at") != null) {
                msg.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
            }
            return msg;
        }, messageId);
        return results.isEmpty() ? null : results.get(0);
    }

    public int countUnreadMessages(int userId) {
        String sql = """
            SELECT COUNT(*) 
            FROM Messages 
            WHERE receiver_user_id = ? AND is_read = 0
            """;
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }
}
