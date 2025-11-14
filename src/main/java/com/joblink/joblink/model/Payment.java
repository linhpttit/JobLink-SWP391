package com.joblink.joblink.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
    private Integer paymentId;
    private Integer invoiceId;
    private String provider;
    private String txRef;
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private String paymentDetails;
    private LocalDateTime createdAt;
}
