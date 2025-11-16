package com.joblink.joblink.repository;

import com.joblink.joblink.entity.CompanyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CompanyReviewRepository extends JpaRepository<CompanyReview, Long> {
    
    @Query("SELECT cr FROM CompanyReview cr " +
           "LEFT JOIN FETCH cr.seeker s " +
           "LEFT JOIN FETCH cr.employer e " +
           "ORDER BY cr.createdAt DESC")
    List<CompanyReview> findAllWithSeekerAndEmployer();
    
    // Native query để update is_deleted nếu column tồn tại
    @Modifying
    @Transactional
    @Query(value = "UPDATE CompanyReviews SET is_deleted = :isDeleted WHERE review_id = :id", nativeQuery = true)
    int updateIsDeleted(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);
}
