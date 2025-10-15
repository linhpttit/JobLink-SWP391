// Giả sử file của bạn đang có những trường này
 package com.joblink.joblink.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSessionDTO {
    private int userId;
    private String email;
    private String username;
    private String role;
    private String fullName;
    private String avatarUrl;

    // ===> THÊM TRƯỜNG NÀY VÀO <===
    private String googleId;
}