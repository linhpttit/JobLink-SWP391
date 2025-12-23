package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @Column(name = "invoice_id", nullable = false)
    private Integer invoiceId;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "tx_ref", nullable = false, length = 200, unique = true)
    private String txRef;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_details", columnDefinition = "NVARCHAR(MAX)")
    private String paymentDetails;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}