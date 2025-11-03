package com.joblink.joblink.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// SỬA LỖI: Import đúng tên entity
import com.joblink.joblink.entity.PremiumPackages;

@Repository
// SỬA LỖI: Tên entity là PremiumPackages
public interface PremiumPackageRepository extends JpaRepository<PremiumPackages, Integer> {

    // SỬA LỖI: Kiểu trả về là PremiumPackages
    List<PremiumPackages> findByUserTypeAndIsActiveTrueOrderByPriceAsc(String userType);

    // SỬA LỖI: Kiểu trả về là PremiumPackages
    Optional<PremiumPackages> findByCodeAndIsActiveTrue(String code);
    
    List<PremiumPackages> findByUserTypeAndIsActiveOrderByPriceAsc(String userType, Boolean isActive);
    
}