package com.joblink.joblink.dto;

import java.io.Serializable;

public class UserSessionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String email;
    private String username;
    private String role;
    private String fullName;
    private String avatarUrl;

    // Constructors
    public UserSessionDTO() {}

    public UserSessionDTO(Integer userId, String email, String username, String role,
                          String fullName, String avatarUrl) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}