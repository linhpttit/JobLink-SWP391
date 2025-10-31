package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    // Keep existing methods
    List<Invoice> findBySeekerIdOrderByIssuedAtDesc(Integer seekerId);

    @Query("SELECT i FROM Invoice i WHERE i.seekerId = :seekerId AND i.status = 'PAID' ORDER BY i.paidAt DESC")
    List<Invoice> findPaidInvoicesBySeeker(@Param("seekerId") Integer seekerId);

    // *** ADD THIS METHOD SIGNATURE ***
    Optional<Invoice> findByInvoiceIdAndUserId(Integer invoiceId, Integer userId);

}