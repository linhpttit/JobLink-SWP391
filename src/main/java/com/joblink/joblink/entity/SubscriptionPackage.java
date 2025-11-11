package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity SubscriptionPackage - Các gói đăng ký Premium
 */
@Entity
@Table(name = "SubscriptionPackage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "package_name", nullable = false, length = 100)
    private String packageName;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "price", nullable = false)
    private Long price; // Giá tiền (VND)

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays; // Số ngày có hiệu lực

    @Column(name = "tier_level", nullable = false)
    private Integer tierLevel; // Cấp độ tier (1=Basic, 2=Premium, 3=Enterprise)

    @Column(name = "features", columnDefinition = "NVARCHAR(MAX)")
    private String features; // Các tính năng (JSON format)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
