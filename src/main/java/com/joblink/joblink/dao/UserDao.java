package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao {
    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* =======================
       Đăng ký bằng SP có sẵn
       ======================= */
    public void register(String email, String rawPassword, String role) {
        String sql = "EXEC dbo.sp_User_Register @Email=?, @RawPassword=?, @Role=?";
        jdbc.update(sql, email, rawPassword, role);
    }

    /* ==============
       ĐĂNG NHẬP
       ============== */
    public User login(String email, String rawPassword) {
        String sql = """
                SELECT user_id, email, role, username, url_avt, enabled, created_at
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
                // DB không có full_name → tạm thời dùng username làm fullName
                u.setFullName(rs.getString("username"));
                u.setUsername(rs.getString("username"));
                u.setAvatarUrl(rs.getString("url_avt"));
                u.setEnabled(rs.getObject("enabled") != null && rs.getBoolean("enabled"));
                // created_at có thể map nếu bạn thêm vào model
                return u;
            }, email, rawPassword);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* ===========
       TRA CỨU
       =========== */
    public User findByEmailAndPassword(String email, String rawPassword) {
        // để tương thích với những nơi còn gọi hàm cũ
        return login(email, rawPassword);
    }

    public User findByEmail(String email) {
        String sql = """
                SELECT user_id, email, role, username, url_avt, enabled, created_at
                FROM dbo.Users WHERE email = ?
                """;
        try {
            return jdbc.queryForObject(sql, (rs, rn) -> {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setFullName(rs.getString("username"));
                u.setUsername(rs.getString("username"));
                u.setAvatarUrl(rs.getString("url_avt"));
                u.setEnabled(rs.getObject("enabled") != null && rs.getBoolean("enabled"));
                return u;
            }, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User findById(int id) {
        String sql = """
                SELECT user_id, email, role, username, url_avt, enabled, created_at
                FROM dbo.Users WHERE user_id = ?
                """;
        return jdbc.query(sql, rs -> {
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setFullName(rs.getString("username"));
                u.setUsername(rs.getString("username"));
                u.setAvatarUrl(rs.getString("url_avt"));
                u.setEnabled(rs.getObject("enabled") != null && rs.getBoolean("enabled"));
                return u;
            }
            return null;
        }, id);
    }

    public boolean emailExists(String email) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dbo.Users WHERE email = ?", Integer.class, email);
        return cnt != null && cnt > 0;
    }

    public int resetPassword(String email, String rawPassword) {
        String sql = "UPDATE dbo.Users SET password_hash = HASHBYTES('SHA2_256', ?) WHERE email = ?";
        return jdbc.update(sql, rawPassword, email);
    }
}
