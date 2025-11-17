package com.joblink.joblink.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private String status;
    private String message;
    private String orderId; // Chính là txnRef
    private Integer paymentId;
    private BigDecimal amount;

    // Thông tin để tạo QR
    private String qrCodeUrl;
    private String bankName;
    private String accountNumber;
    private String accountOwner;
}