package com.joblink.joblink.auth.model;



import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobPosting {
    private Integer jobId;
    private int employerId;
    private Integer categoryId;
    private String title;
    private String description;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private LocalDateTime postedAt;

    // Các trường giả định bạn sẽ thêm vào DB hoặc join để lấy
    private String jobType = "Full Time"; // Mặc định
    private boolean isFeatured = true; // Mặc định
    private LocalDateTime expireAt;
    private String educationLevel = "Graduation"; // Mặc định
    private String experienceYears = "2-3"; // Mặc định
    private List<String> responsibilities;
    private List<String> requirements;

    // Thuộc tính join từ EmployerProfile
    private String companyName;
}