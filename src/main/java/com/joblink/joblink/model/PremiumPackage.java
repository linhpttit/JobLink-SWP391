package com.joblink.joblink.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PremiumPackage {
    private Integer packageId;
    private String code;
    private String name;
    private String userType;
    private BigDecimal price;
    private Integer durationDays;

    // Employer features
    private Integer maxActiveJobs;
    private Integer boostCredits;
    private Integer candidateViews;
    private Boolean highlight;

    // JobSeeker features
    private Boolean cvTemplatesAccess;
    private Boolean messagingEnabled;
    private Boolean seekerNetworkingEnabled;
    private Integer pdfExportLimit;

    private String features;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
