
package com.joblink.joblink.repository;

import com.joblink.joblink.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByTxRef(String txRef);

    Optional<Payment> findByInvoiceId(Integer invoiceId);

    boolean existsByTxRef(String txRef);
}