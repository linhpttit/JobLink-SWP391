package com.joblink.joblink.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentHistoryRepository extends JpaRepository<com.joblink.joblink.entity.Payment, Integer> {

    interface Row {
        Integer getPaymentId();
        Integer getInvoiceId();
        String  getProvider();
        String  getPaymentMethod();
        String  getTxRef();
        BigDecimal getAmount();
        String  getPayStatus();
        OffsetDateTime getPaidAt();

        Integer getSubscriptionId();
        String  getSubStatus();

        Integer getPackageId();
        String  getPackageCode();
        String  getPackageName();
        Integer getDurationDays();
    }

    @Query(
        value = """
            SELECT  p.payment_id     AS paymentId,
                    p.invoice_id     AS invoiceId,
                    p.provider       AS provider,
                    p.payment_method AS paymentMethod,
                    p.tx_ref         AS txRef,
                    p.amount         AS amount,
                    p.status         AS payStatus,
                    p.created_at     AS paidAt,

                    s.subscription_id AS subscriptionId,
                    s.status          AS subStatus,

                    pkg.package_id    AS packageId,
                    pkg.code          AS packageCode,
                    pkg."name"        AS packageName,
                    pkg.duration_days AS durationDays
            FROM public.payment p
            JOIN public.invoice i               ON i.invoice_id = p.invoice_id
            JOIN public.premium_subscriptions s ON s.invoice_id = i.invoice_id
            JOIN public.premium_packages pkg    ON pkg.package_id = s.package_id
            WHERE s.user_id = :userId
            ORDER BY p.created_at DESC
            """,
        countQuery = """
            SELECT COUNT(1)
            FROM public.payment p
            JOIN public.invoice i               ON i.invoice_id = p.invoice_id
            JOIN public.premium_subscriptions s ON s.invoice_id = i.invoice_id
            WHERE s.user_id = :userId
            """,
        nativeQuery = true
    )
    Page<Row> findHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
}
