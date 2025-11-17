package com.joblink.joblink.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Invoice {
    private Integer invoiceId;
    private Integer userId;
    private Integer employerId;
    private Integer seekerId;
    private Integer subscriptionId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime dueAt;
    private LocalDateTime paidAt;

    // Additional fields for display
    private String packageName;
    private String userName;
}

