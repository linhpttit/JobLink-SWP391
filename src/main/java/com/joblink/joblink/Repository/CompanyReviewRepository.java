package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.CompanyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CompanyReviewRepository extends JpaRepository<CompanyReview, Long> {
<<<<<<< HEAD
    
    @Query("SELECT cr FROM CompanyReview cr " +
           "LEFT JOIN FETCH cr.seeker s " +
           "LEFT JOIN FETCH cr.employer e " +
           "ORDER BY cr.createdAt DESC")
    List<CompanyReview> findAllWithSeekerAndEmployer();
    
=======

    @Query("SELECT cr FROM CompanyReview cr " +
            "LEFT JOIN FETCH cr.seeker s " +
            "LEFT JOIN FETCH cr.employer e " +
            "ORDER BY cr.createdAt DESC")
    List<CompanyReview> findAllWithSeekerAndEmployer();

>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141
    // Native query để update is_deleted nếu column tồn tại
    @Modifying
    @Transactional
    @Query(value = "UPDATE CompanyReviews SET is_deleted = :isDeleted WHERE review_id = :id", nativeQuery = true)
    int updateIsDeleted(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);
<<<<<<< HEAD
}
=======
}
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141
