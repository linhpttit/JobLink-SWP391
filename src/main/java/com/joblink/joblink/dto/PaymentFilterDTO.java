package com.joblink.joblink.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentFilterDTO {
    private String search; // Tìm kiếm theo mã giao dịch, email, công ty
    private String paymentStatus; // SUCCESS, FAILED, PENDING, CANCELLED
    private Integer tierLevel; // 1, 2, 3
    private String paymentMethod; // ATM, VISA, etc.
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Long minAmount;
    private Long maxAmount;
    
    // Pagination parameters
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
