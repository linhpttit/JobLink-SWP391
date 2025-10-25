package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.PremiumSubscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<PremiumSubscriptions, Integer> {

    // SỬA LỖI: Tên entity là PremiumSubscriptions (có 's')
    @Query("SELECT s FROM PremiumSubscriptions s WHERE s.seekerId = :seekerId " +
            "AND s.isActive = true AND s.status = 'ACTIVE' " +
            "AND s.endDate >= CURRENT_TIMESTAMP ORDER BY s.endDate DESC")
    List<PremiumSubscriptions> findActiveBySeekerIdendDate(@Param("seekerId") Integer seekerId);

    Optional<PremiumSubscriptions> findTopBySeekerIdAndStatusAndIsActiveTrueOrderByEndDateDesc(
            Integer seekerId, String status
    );
}