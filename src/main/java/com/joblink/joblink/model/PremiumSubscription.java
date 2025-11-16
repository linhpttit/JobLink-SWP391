
package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PremiumSubscription {
    private Integer subscriptionId;
    private Integer userId;
    private Integer employerId;
    private Integer seekerId;
    private Integer packageId;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for display
    private String packageName;
    private String packageCode;
}
