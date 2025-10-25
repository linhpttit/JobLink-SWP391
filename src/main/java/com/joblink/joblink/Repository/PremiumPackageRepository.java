package com.joblink.joblink.Repository;

// SỬA LỖI: Import đúng tên entity
import com.joblink.joblink.entity.PremiumPackages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// SỬA LỖI: Tên entity là PremiumPackages
public interface PremiumPackageRepository extends JpaRepository<PremiumPackages, Integer> {

    // SỬA LỖI: Kiểu trả về là PremiumPackages
    List<PremiumPackages> findByUserTypeAndIsActiveTrueOrderByPriceAsc(String userType);

    // SỬA LỖI: Kiểu trả về là PremiumPackages
    Optional<PremiumPackages> findByCodeAndIsActiveTrue(String code);
}