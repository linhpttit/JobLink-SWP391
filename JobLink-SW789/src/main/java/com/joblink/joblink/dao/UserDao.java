package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
        // Kiểm tra tham số đầu vào
        if (email == null || rawPassword == null || email.trim().isEmpty() || rawPassword.trim().isEmpty()) {
            return null;
        }
        
        String sql = """
                SELECT user_id, email, role, username, url_avt, created_at
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
                u.setEnabled(true); // Mặc định enabled = true vì DB không có cột này
                // created_at có thể map nếu bạn thêm vào model
                return u;
            }, email.trim(), rawPassword.trim());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            System.err.println("[UserDao] Login SQL error: " + e.getMessage());
            System.err.println("SQL: " + sql);
            System.err.println("Parameters: email=" + email + ", password=[HIDDEN]");
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
                SELECT user_id, email, role, username, url_avt, created_at
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
                u.setEnabled(true); // Mặc định enabled = true vì DB không có cột này
                return u;
            }, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User findById(int id) {
        String sql = """
                SELECT user_id, email, role, username, url_avt, created_at
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
                u.setEnabled(true); // Mặc định enabled = true vì DB không có cột này
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

    /**
     * Kiểm tra cấu trúc bảng Users và test kết nối database
     */
    public void checkDatabaseSchema() {
        try {
            // Kiểm tra bảng Users có tồn tại không
            String checkTableSql = """
                SELECT COUNT(*) 
                FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'Users'
                """;
            Integer tableExists = jdbc.queryForObject(checkTableSql, Integer.class);
            System.out.println("[UserDao] Table Users exists: " + (tableExists != null && tableExists > 0));
            
            // Kiểm tra các cột trong bảng Users
            String checkColumnsSql = """
                SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'Users'
                ORDER BY ORDINAL_POSITION
                """;
            jdbc.query(checkColumnsSql, (rs) -> {
                System.out.println("[UserDao] Column: " + rs.getString("COLUMN_NAME") + 
                                 " Type: " + rs.getString("DATA_TYPE") + 
                                 " Nullable: " + rs.getString("IS_NULLABLE"));
            });
            
        } catch (Exception e) {
            System.err.println("[UserDao] Database schema check error: " + e.getMessage());
        }
    }

    /**
     * Test câu SQL đăng nhập với dữ liệu mẫu
     */
    public void testLoginQuery(String testEmail, String testPassword) {
        try {
            System.out.println("[UserDao] Testing login query...");
            System.out.println("Test email: " + testEmail);
            System.out.println("Test password: [HIDDEN]");
            
            // Test câu SQL cơ bản trước
            String basicSql = "SELECT COUNT(*) FROM dbo.Users WHERE email = ?";
            Integer count = jdbc.queryForObject(basicSql, Integer.class, testEmail);
            System.out.println("[UserDao] Users found with email: " + count);
            
            // Test câu SQL với HASHBYTES
            String hashSql = """
                SELECT user_id, email, role, username, url_avt, created_at
                FROM dbo.Users
                WHERE email = ?
                  AND password_hash = HASHBYTES('SHA2_256', ?)
                """;
            
            User result = jdbc.queryForObject(hashSql, (rs, rowNum) -> {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setFullName(rs.getString("username"));
                u.setUsername(rs.getString("username"));
                u.setAvatarUrl(rs.getString("url_avt"));
                u.setEnabled(true); // Mặc định enabled = true vì DB không có cột này
                return u;
            }, testEmail, testPassword);
            
            if (result != null) {
                System.out.println("[UserDao] Login test SUCCESS: " + result.getEmail() + " role=" + result.getRole());
            } else {
                System.out.println("[UserDao] Login test FAILED: No user found");
            }
            
        } catch (Exception e) {
            System.err.println("[UserDao] Login test error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int resetPassword(String email, String rawPassword) {
        if (email == null || rawPassword == null || email.trim().isEmpty() || rawPassword.trim().isEmpty()) {
            return 0;
        }
        
        String sql = "UPDATE dbo.Users SET password_hash = HASHBYTES('SHA2_256', ?) WHERE email = ?";
        try {
            return jdbc.update(sql, rawPassword.trim(), email.trim());
        } catch (Exception e) {
            System.err.println("[UserDao] Reset password SQL error: " + e.getMessage());
            System.err.println("SQL: " + sql);
            System.err.println("Parameters: email=" + email + ", password=[HIDDEN]");
            return 0;
        }
    }

    /**
     * Set the user's hidden/active flag. Assumes there is a column `is_hidden` or `enabled`.
     * If your Users table uses a different column, adjust SQL accordingly.
     */
    public int setHidden(int userId, boolean hidden) {
        try {
            // Try to update `is_hidden` column; fallback to `enabled` if necessary
            String sql = "UPDATE dbo.Users SET is_hidden = ? WHERE user_id = ?";
            return jdbc.update(sql, hidden ? 1 : 0, userId);
        } catch (Exception e) {
            try {
                String sql2 = "UPDATE dbo.Users SET enabled = ? WHERE user_id = ?";
                return jdbc.update(sql2, hidden ? 0 : 1, userId);
            } catch (Exception ex) {
                System.err.println("[UserDao] setHidden error: " + ex.getMessage());
                return 0;
            }
        }
    }

    /**
     * Update a user's notification preference for followed companies posts.
     * This assumes a simple user_settings table or a column on Users table.
     * Adjust SQL to match your schema.
     */
    public int updateNotificationPref(int userId, boolean emailFollowPosts) {
        try {
            String sql = "UPDATE dbo.Users SET email_follow_posts = ? WHERE user_id = ?";
            return jdbc.update(sql, emailFollowPosts ? 1 : 0, userId);
        } catch (Exception e) {
            System.err.println("[UserDao] updateNotificationPref error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Unblock a company from user's blocked list (implementation depends on schema).
     * We'll attempt to delete from a hypothetical table user_blocked_companies(user_id, company_id).
     */
    public int unblockCompany(int userId, int companyId) {
        try {
            String sql = "DELETE FROM dbo.user_blocked_companies WHERE user_id = ? AND company_id = ?";
            return jdbc.update(sql, userId, companyId);
        } catch (Exception e) {
            System.err.println("[UserDao] unblockCompany error: " + e.getMessage());
            return 0;
        }
    }
}
