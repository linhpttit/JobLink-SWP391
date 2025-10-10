package com.joblink.joblink.auth.model;

import lombok.Data;

/* ===== Basic info update ===== */
@Data
public class BasicInfoUpdate {
    private String fullName;
    private String gender;
    private String location;
    private String headline;
    private Integer experienceYears;
    private String about;
    private String phone;
    private String email;
    private String username;
}
