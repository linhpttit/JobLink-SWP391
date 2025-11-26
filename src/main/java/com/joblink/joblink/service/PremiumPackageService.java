package com.joblink.joblink.service;



import com.joblink.joblink.entity.PremiumPackages;
import com.joblink.joblink.repository.PremiumPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PremiumPackageService implements IPremiumPackageService {

    private final PremiumPackageRepository packageRepository;

    // Hằng số cho user type, "SEEKER" hoặc "JobSeeker" tùy bạn định nghĩa trong DB
    private static final String USER_TYPE_SEEKER = "JOBSEEKER";

    @Override
    public List<PremiumPackages> getActiveSeekerPackages() {
        return packageRepository. findByUserTypeAndIsActiveTrueOrderByPriceAsc(USER_TYPE_SEEKER);
    }

    @Override
    public PremiumPackages getPackageById(Integer packageId) {
        return packageRepository.findById(packageId)
                .orElse(null); // Controller có check null
    }
}