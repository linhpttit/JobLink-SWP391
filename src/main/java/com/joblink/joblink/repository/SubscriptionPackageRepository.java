package com.joblink.joblink.repository;

import com.joblink.joblink.entity.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, Long> {
    
    List<SubscriptionPackage> findByIsActiveTrueOrderByTierLevelAsc();
    
    List<SubscriptionPackage> findByIsActiveOrderByTierLevelAsc(Boolean isActive);
    
    Optional<SubscriptionPackage> findByTierLevel(Integer tierLevel);
}
