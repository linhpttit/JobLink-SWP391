package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompanyReview {
    private Long reviewId;
    private Integer seekerId;
    private Long employerId;
    private Byte rating;
    private String comment;
    private LocalDateTime createdAt;

    // Có thể thêm tên seeker hoặc tên công ty nếu muốn hiển thị ngoài giao diện
    private String seekerName;
    private String companyName;
}

