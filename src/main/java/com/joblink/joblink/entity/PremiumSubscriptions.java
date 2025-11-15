package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "PremiumSubscriptions")
@Data
public class PremiumSubscriptions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Integer subscriptionId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "employer_id")
    private Integer employerId;

    @Column(name = "seeker_id")
    private Integer seekerId;

    @Column(name = "package_id", nullable = false)
    private Integer packageId;

    @Column(name = "invoice_id", nullable = false)
    private Integer invoiceId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    public String getPackageName() {
        return null; // Will be loaded via join
    }
}