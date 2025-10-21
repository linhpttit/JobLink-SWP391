// File: JobSeekerProfile.java (Đảm bảo file này có nội dung như sau)
package com.joblink.joblink.model; // Hoặc package tương ứng của bạn

import lombok.Data;
import java.time.LocalDate;

@Data
public class JobSeekerProfile {
    private Integer seekerId;
    private int userId;
    private String fullname;
    private String gender;
    private String location;
    private String headline;
    private Integer experienceYears;
    private String about;
    private String email;
    private String phoneNumber; // Khớp với phương thức getPhoneNumber()
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private int completionPercentage;

    // === THUỘC TÍNH BẠN CẦN THÊM VÀO ĐÂY ===
    private boolean receiveInvitations;
}