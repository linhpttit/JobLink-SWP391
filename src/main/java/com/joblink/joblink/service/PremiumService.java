package com.joblink.joblink.service;

import com.joblink.joblink.dao.PremiumPackageDao;
import com.joblink.joblink.dao.PremiumSubscriptionDao;
import com.joblink.joblink.model.PremiumPackage;
import com.joblink.joblink.model.PremiumSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PremiumService {
    private final PremiumPackageDao premiumPackageDao;
    private final PremiumSubscriptionDao premiumSubscriptionDao;

    public PremiumService(PremiumPackageDao premiumPackageDao, PremiumSubscriptionDao premiumSubscriptionDao) {
        this.premiumPackageDao = premiumPackageDao;
        this.premiumSubscriptionDao = premiumSubscriptionDao;
    }

    public List<PremiumPackage> getJobSeekerPackages() {
        return premiumPackageDao.findByUserType("JOBSEEKER");
    }

    public List<PremiumPackage> getEmployerPackages() {
        return premiumPackageDao.findByUserType("EMPLOYER");
    }

    public PremiumPackage getPackageById(int packageId) {
        return premiumPackageDao.findById(packageId);
    }

    public PremiumPackage getPackageByCode(String code) {
        return premiumPackageDao.findByCode(code);
    }

    public PremiumSubscription getActiveSubscription(int userId) {
        return premiumSubscriptionDao.findActiveByUserId(userId);
    }

    public boolean hasPremiumAccess(int userId) {
        PremiumSubscription subscription = premiumSubscriptionDao.findActiveByUserId(userId);
        return subscription != null;
    }

    public boolean hasFeature(int userId, String feature) {
        PremiumSubscription subscription = premiumSubscriptionDao.findActiveByUserId(userId);
        if (subscription == null) {
            return false;
        }

        PremiumPackage pkg = premiumPackageDao.findById(subscription.getPackageId());
        if (pkg == null) {
            return false;
        }

        return switch (feature) {
            case "cv_templates" -> pkg.getCvTemplatesAccess() != null && pkg.getCvTemplatesAccess();
            case "messaging" -> pkg.getMessagingEnabled() != null && pkg.getMessagingEnabled();
            case "networking" -> pkg.getSeekerNetworkingEnabled() != null && pkg.getSeekerNetworkingEnabled();
            default -> false;
        };
    }

    @Transactional
    public PremiumSubscription createSubscription(int userId, Integer employerId, Integer seekerId, int packageId) {
        PremiumPackage pkg = premiumPackageDao.findById(packageId);
        if (pkg == null) {
            throw new IllegalArgumentException("Package not found");
        }

        PremiumSubscription subscription = new PremiumSubscription();
        subscription.setUserId(userId);
        subscription.setEmployerId(employerId);
        subscription.setSeekerId(seekerId);
        subscription.setPackageId(packageId);
        subscription.setStatus("ACTIVE");
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(pkg.getDurationDays()));
        subscription.setIsActive(true);

        int subscriptionId = premiumSubscriptionDao.create(subscription);
        subscription.setSubscriptionId(subscriptionId);

        return subscription;
    }
}

