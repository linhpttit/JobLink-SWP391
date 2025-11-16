package com.joblink.joblink.controller;

import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.service.SubscriptionExpiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller để quản lý subscription expiry
 */
@RestController
@RequestMapping("/api/admin/subscription")
public class SubscriptionExpiryController {

    @Autowired
    private SubscriptionExpiryService subscriptionExpiryService;

    /**
     * Manual trigger để reset tất cả subscription hết hạn
     * POST /api/admin/subscription/reset-expired
     */
    @PostMapping("/reset-expired")
    public ResponseEntity<Map<String, Object>> resetExpiredSubscriptions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            subscriptionExpiryService.resetExpiredSubscriptions();
            
            response.put("success", true);
            response.put("message", "Đã reset tất cả subscription hết hạn về Free tier");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Reset subscription của một employer cụ thể
     * POST /api/admin/subscription/reset-user/{userId}
     */
    @PostMapping("/reset-user/{userId}")
    public ResponseEntity<Map<String, Object>> resetUserSubscription(@PathVariable Integer userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = subscriptionExpiryService.resetEmployerSubscription(userId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Đã reset subscription của userId: " + userId + " về Free tier");
            } else {
                response.put("success", false);
                response.put("message", "Không thể reset - user không tồn tại hoặc subscription chưa hết hạn");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Xem danh sách subscription sắp hết hạn
     * GET /api/admin/subscription/expiring
     */
    @GetMapping("/expiring")
    public ResponseEntity<Map<String, Object>> getExpiringSubscriptions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Employer> expiringEmployers = subscriptionExpiryService.getExpiringSubscriptions();
            
            response.put("success", true);
            response.put("count", expiringEmployers.size());
            response.put("data", expiringEmployers.stream().map(employer -> {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", employer.getUser().getUserId());
                item.put("email", employer.getUser().getEmail());
                item.put("companyName", employer.getCompanyName());
                item.put("tierLevel", employer.getTierLevel());
                item.put("expiresAt", employer.getSubscriptionExpiresAt());
                return item;
            }).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
