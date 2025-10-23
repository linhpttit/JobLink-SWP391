package com.joblink.joblink.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PremiumPackages")
@Data
public class PremiumPackages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Integer packageId;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "max_active_jobs")
    private Integer maxActiveJobs;

    @Column(name = "boost_credits")
    private Integer boostCredits;

    @Column(name = "candidate_views")
    private Integer candidateViews;

    @Column(name = "highlight")
    private Boolean highlight;

    @Column(name = "cv_templates_access", nullable = false)
    private Boolean cvTemplatesAccess;

    @Column(name = "messaging_enabled", nullable = false)
    private Boolean messagingEnabled;

    @Column(name = "seeker_networking_enabled", nullable = false)
    private Boolean seekerNetworkingEnabled;

    @Column(name = "pdf_export_limit")
    private Integer pdfExportLimit;

    @Column(name = "features", columnDefinition = "NVARCHAR(MAX)")
    private String features;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

// ==========================================

// ==========================================

