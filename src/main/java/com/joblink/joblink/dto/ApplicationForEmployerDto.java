package com.joblink.joblink.dto;

import lombok.Data;

/**
 * DTO hiển thị hồ sơ ứng viên cho employer
 * - Gộp thông tin từ Application, JobSeekerProfile và JobsPosting
 * - Có field bookmarked để hiển thị trạng thái lưu hồ sơ
 */
@Data
public class ApplicationForEmployerDto {
    private Long applicationId;
    private String seekerFullname;
    private String location;
    private Integer experienceYears;
    private String degree;
    private String email;
    private String phone;
    private String jobTitle;
    private String cvUrl;
    private String status; // trạng thái application: submitted, reviewed, rejected, hired
    private boolean bookmarked; // true nếu employer đã lưu hồ sơ
    private String avatar; // 🟢 THÊM AVATAR
}