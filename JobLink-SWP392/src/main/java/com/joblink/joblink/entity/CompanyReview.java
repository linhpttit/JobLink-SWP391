package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ với bảng CompanyReviews
 */
@Entity
@Table(name = "CompanyReviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    // 🔹 Người đánh giá (Job Seeker)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", nullable = false)
    private JobSeekerProfile seeker;

    // 🔹 Công ty được đánh giá (Employer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    // 🔹 Điểm đánh giá (1–5)
    @Column(name = "rating", nullable = false)
    private Byte rating;

    // 🔹 Bình luận của người dùng
    @Column(name = "comment", columnDefinition = "NVARCHAR(1000)")
    private String comment;

    // 🔹 Ngày tạo (tự động set theo DB default hoặc tự set từ code)
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // 🔹 Đánh dấu đã xóa (soft delete) - sử dụng @Transient để tránh lỗi nếu column chưa tồn tại
    @Transient
    private Boolean isDeleted = false;
}
