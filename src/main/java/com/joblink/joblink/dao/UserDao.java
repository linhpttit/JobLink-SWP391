package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {
    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Đăng kí: gọi stored procedure sp_User_Register (đã kiểm tra policy & trùng email ở DB) */
    public void register(String email, String rawPassword, String role) {
        String sql = "EXEC dbo.sp_User_Register @Email=?, @RawPassword=?, @Role=?";
        jdbc.update(sql, email, rawPassword, role);
    }

    /** Đăng nhập: so sánh SHA-256 bằng HASHBYTES ở SQL Server */
    public User login(String email, String rawPassword) {
        String sql = """
            SELECT user_id, email, role
            FROM dbo.Users
            WHERE email = ?
              AND password_hash = HASHBYTES('SHA2_256', ?)
        """;
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                return u;
            }, email, rawPassword);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** Kiểm tra email tồn tại (để báo sớm ở UI, dù DB đã chặn unique) */
    public boolean emailExists(String email) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dbo.Users WHERE email = ?", Integer.class, email);
        return cnt != null && cnt > 0;
    }

    // UserDao.java
    public User findById(int id) {
        String sql = "SELECT user_id, email, role FROM Users WHERE user_id = ?";
        return jdbc.query(sql, rs -> {
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                return u;
            }
            return null;
        }, id);
    }

    public int resetPassword(String email, String rawPassword) {
        String sql = "UPDATE Users SET password_hash = HASHBYTES('SHA2_256', ?) WHERE email = ?";
        return jdbc.update(sql, rawPassword, email);
    }

}

