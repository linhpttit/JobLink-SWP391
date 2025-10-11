package com.joblink.joblink.auth.model;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private int userId;
    private String email;
    private String role;
    private String fullName;
    private String username;
    private String avatarUrl;
    private boolean enabled;
    private LocalDateTime createdAt;



}
