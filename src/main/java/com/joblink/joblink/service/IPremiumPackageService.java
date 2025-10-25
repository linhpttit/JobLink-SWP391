package com.joblink.joblink.service;
import com.joblink.joblink.entity.PremiumPackages;
import java.util.List;

public interface IPremiumPackageService {
    List<PremiumPackages> getActiveSeekerPackages();
    PremiumPackages getPackageById(Integer packageId);
}