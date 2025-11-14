package com.joblink.joblink.repository;

import com.joblink.joblink.entity.PaymentTransaction;
import com.joblink.joblink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long>, JpaSpecificationExecutor<PaymentTransaction> {
    
    Optional<PaymentTransaction> findByVnpayTxnRef(String vnpayTxnRef);
    
    List<PaymentTransaction> findByUserOrderByCreatedAtDesc(User user);
    
    List<PaymentTransaction> findByUserAndPaymentStatusOrderByCreatedAtDesc(User user, String paymentStatus);
    
    List<PaymentTransaction> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
    
    List<PaymentTransaction> findByUserUserIdAndPaymentStatusOrderByCreatedAtDesc(Integer userId, String paymentStatus);
    
    @Query("SELECT pt FROM PaymentTransaction pt " +
           "LEFT JOIN FETCH pt.user u " +
           "LEFT JOIN FETCH pt.subscriptionPackage sp " +
           "ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();
}
