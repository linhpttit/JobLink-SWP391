package com.joblink.joblink.auth.model;

import lombok.Data;

@Data
public class User {
    private int userId;
    private String email;
    private String fullName;
    private String role;
    private String status;
}
