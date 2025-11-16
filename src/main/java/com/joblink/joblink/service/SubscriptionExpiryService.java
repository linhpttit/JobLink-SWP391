package com.joblink.joblink.service;

import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.Repository.EmployerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service để xử lý việc hết hạn subscription tự động
 */
@Service
public class SubscriptionExpiryService {

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private PaymentService paymentService;

    /**
     * Scheduled task chạy mỗi giờ để kiểm tra và reset các subscription hết hạn
     * Cron: 0 0 * * * * = chạy vào đầu mỗi giờ
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void resetExpiredSubscriptions() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Tìm tất cả employer có subscription hết hạn và tier > 0 (không phải Free)
            List<Employer> expiredEmployers = employerRepository.findExpiredSubscriptions(now);
            
            if (!expiredEmployers.isEmpty()) {
                System.out.println("Found " + expiredEmployers.size() + " expired subscriptions to reset");
                
                for (Employer employer : expiredEmployers) {
                    Integer userId = employer.getUser().getUserId();
                    
                    // Tìm gói còn hạn cao nhất từ PaymentTransaction
                    Map<String, Object> validSubscription = paymentService.findHighestValidSubscription(userId);
                    
                    if ((Boolean) validSubscription.get("found")) {
                        // Có gói còn hạn, reset về gói đó
                        Integer newTierLevel = (Integer) validSubscription.get("tierLevel");
                        LocalDateTime newExpiryDate = (LocalDateTime) validSubscription.get("expiryDate");
                        
                        employer.setTierLevel(newTierLevel);
                        employer.setSubscriptionExpiresAt(newExpiryDate);
                        
                        System.out.println("Auto-reset expired subscription for userId: " + userId + 
                                         " to tier " + newTierLevel + " (expires: " + newExpiryDate + ")");
                    } else {
                        // Không có gói nào còn hạn, reset về Free tier
                        employer.setTierLevel(0);
                        employer.setSubscriptionExpiresAt(null);
                        
                        System.out.println("Auto-reset expired subscription for userId: " + userId + " to Free tier");
                    }
                }
                
                // Batch save để tối ưu performance
                employerRepository.saveAll(expiredEmployers);
                
                System.out.println("Successfully reset " + expiredEmployers.size() + " expired subscriptions");
            }
            
        } catch (Exception e) {
            System.err.println("Error in resetExpiredSubscriptions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Method để manual reset một employer cụ thể
     */
    @Transactional
    public boolean resetEmployerSubscription(Integer userId) {
        try {
            Employer employer = employerRepository.findByUserId(userId).orElse(null);
            if (employer == null) {
                return false;
            }
            
            // Kiểm tra xem có hết hạn không
            LocalDateTime now = LocalDateTime.now();
            boolean isExpired = employer.getSubscriptionExpiresAt() != null && 
                              employer.getSubscriptionExpiresAt().isBefore(now);
            
            if (isExpired && employer.getTierLevel() != null && employer.getTierLevel() > 0) {
                // Tìm gói còn hạn cao nhất từ PaymentTransaction
                Map<String, Object> validSubscription = paymentService.findHighestValidSubscription(userId);
                
                if ((Boolean) validSubscription.get("found")) {
                    // Có gói còn hạn, reset về gói đó
                    Integer newTierLevel = (Integer) validSubscription.get("tierLevel");
                    LocalDateTime newExpiryDate = (LocalDateTime) validSubscription.get("expiryDate");
                    
                    employer.setTierLevel(newTierLevel);
                    employer.setSubscriptionExpiresAt(newExpiryDate);
                    
                    System.out.println("Manual reset expired subscription for userId: " + userId + 
                                     " to tier " + newTierLevel + " (expires: " + newExpiryDate + ")");
                } else {
                    // Không có gói nào còn hạn, reset về Free tier
                    employer.setTierLevel(0);
                    employer.setSubscriptionExpiresAt(null);
                    
                    System.out.println("Manual reset expired subscription for userId: " + userId + " to Free tier");
                }
                
                employerRepository.save(employer);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error in resetEmployerSubscription: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra subscription sắp hết hạn (trong vòng 7 ngày)
     */
    public List<Employer> getExpiringSubscriptions() {
        LocalDateTime sevenDaysFromNow = LocalDateTime.now().plusDays(7);
        return employerRepository.findExpiringSubscriptions(sevenDaysFromNow);
    }
}
