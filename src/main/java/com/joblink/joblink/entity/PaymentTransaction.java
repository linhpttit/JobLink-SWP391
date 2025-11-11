package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity PaymentTransaction - Lịch sử giao dịch thanh toán
 */
@Entity
@Table(name = "PaymentTransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private SubscriptionPackage subscriptionPackage;

    @Column(name = "vnpay_txn_ref", unique = true, length = 100)
    private String vnpayTxnRef; // Mã giao dịch VNPay

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus; // PENDING, SUCCESS, FAILED

    @Column(name = "vnpay_response_code", length = 10)
    private String vnpayResponseCode;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "transaction_info", columnDefinition = "NVARCHAR(500)")
    private String transactionInfo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "tier_upgraded_to")
    private Integer tierUpgradedTo; // Tier level sau khi nâng cấp

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
