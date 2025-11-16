package com.joblink.joblink.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/temp")
public class TempUserController {
    
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    
    public TempUserController(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Tạo user mới với BCrypt password
     * URL: http://localhost:8081/temp/create-user?email=test@test.com&password=123456&role=seeker
     */
    @PostMapping("/create-user")
    public String createUser(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "seeker") String role) {
        
        try {
            // Hash password bằng BCrypt
            String hashedPassword = passwordEncoder.encode(password);
            
            // Insert trực tiếp vào database với BCrypt hash
            String sql = """
                INSERT INTO dbo.Users (email, password_hash, role, username, created_at)
                VALUES (?, ?, ?, ?, GETDATE())
                """;
            
            jdbc.update(sql, email, hashedPassword, role, email);
            
            return "✅ User created successfully!\n" +
                   "Email: " + email + "\n" +
                   "Password: " + password + "\n" +
                   "Role: " + role + "\n" +
                   "Now you can login with these credentials.";
                   
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    /**
     * Reset password của user hiện có sang BCrypt
     * URL: http://localhost:8081/temp/reset-password?email=test@test.com&newPassword=123456
     */
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String newPassword) {
        
        try {
            // Hash password mới bằng BCrypt
            String hashedPassword = passwordEncoder.encode(newPassword);
            
            // Update password trong database
            String sql = "UPDATE dbo.Users SET password_hash = ? WHERE email = ?";
            int rows = jdbc.update(sql, hashedPassword, email);
            
            if (rows > 0) {
                return "✅ Password reset successfully!\n" +
                       "Email: " + email + "\n" +
                       "New Password: " + newPassword + "\n" +
                       "Now you can login with the new password.";
            } else {
                return "❌ User not found with email: " + email;
            }
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    /**
     * Xem danh sách users
     * URL: http://localhost:8081/temp/list-users
     */
    @GetMapping("/list-users")
    public String listUsers() {
        try {
            String sql = "SELECT user_id, email, role FROM dbo.Users";
            StringBuilder result = new StringBuilder("📋 Users in database:\n\n");
            
            jdbc.query(sql, (rs) -> {
                result.append("ID: ").append(rs.getInt("user_id"))
                      .append(" | Email: ").append(rs.getString("email"))
                      .append(" | Role: ").append(rs.getString("role"))
                      .append("\n");
            });
            
            return result.toString();
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
}
