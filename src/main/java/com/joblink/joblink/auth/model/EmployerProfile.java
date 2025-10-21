package com.joblink.joblink.auth.model;


import lombok.Data;

@Data
public class EmployerProfile {
    private Integer employerId;
    private int userId;
    private String companyName;
    private String industry;
    private String location;
    private String phoneNumber;
    private String description;

    // Các trường bổ sung nếu cần
    private String logoUrl;
    private String companySize;
    private String email;
    private String websiteUrl;
}