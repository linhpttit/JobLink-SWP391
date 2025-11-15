package com.joblink.joblink.Repository;

// SỬA LỖI: Import đúng tên entity
import com.joblink.joblink.entity.PremiumPackages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Tìm kiếm theo tên hoặc code (không phân biệt hoa thường)
    @Query("SELECT p FROM PremiumPackages p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PremiumPackages> findByNameOrCodeContainingIgnoreCase(@Param("keyword") String keyword);
}