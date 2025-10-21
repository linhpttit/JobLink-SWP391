package com.joblink.joblink.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO để lưu thông tin user trong HTTP Session
 * Phải implement Serializable để có thể lưu vào session
 *
 * Nếu bạn đã có file này rồi thì KHÔNG CẦN thay đổi gì
 * Chỉ cần đảm bảo có field userId và các getter/setter
 */
@Getter
@Setter
@Data
public class UserSessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;        // Quan trọng: dùng để tìm profile
    private String username;
    private String email;
    private String role;           // "employer" hoặc "jobseeker"
    private String avatarUrl;
    private String fullName;       // Tên hiển thị
    private String GoogleID;
    // Constructor mặc định
    public UserSessionDTO() {
    }

    // Constructor để dễ tạo object khi login
    public UserSessionDTO(Integer userId, String username, String email, String role, String GoogleID) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.GoogleID = GoogleID;
    }
}