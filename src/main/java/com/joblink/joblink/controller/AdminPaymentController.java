package com.joblink.joblink.controller;

import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.repository.EmployerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/payment")
public class AdminPaymentController {

    private final EmployerRepository employerRepository;

    public AdminPaymentController(EmployerRepository employerRepository) {
        this.employerRepository = employerRepository;
    }

    /**
     * API cheat để reset tier user thủ công
     * POST /api/admin/payment/reset-tier
     * Body: { "userId": 1, "tierLevel": 2, "durationDays": 30 }
     */
    @PostMapping("/reset-tier")
    public ResponseEntity<Map<String, Object>> resetUserTier(
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = (Integer) request.get("userId");
            Integer tierLevel = (Integer) request.get("tierLevel");
            Integer durationDays = (Integer) request.getOrDefault("durationDays", 30);

            if (userId == null || tierLevel == null) {
                response.put("success", false);
                response.put("message", "userId và tierLevel là bắt buộc");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate tier level
            if (tierLevel < 0 || tierLevel > 3) {
                response.put("success", false);
                response.put("message", "tierLevel phải từ 0-3 (0=Free, 1=Basic, 2=Premium, 3=Enterprise)");
                return ResponseEntity.badRequest().body(response);
            }

            // Tìm employer profile
            Optional<Employer> employerOpt = employerRepository.findByUserId(userId);
            
            if (employerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy employer profile cho userId: " + userId);
                return ResponseEntity.notFound().build();
            }

            Employer employer = employerOpt.get();
            
            // Cập nhật tier
            employer.setTierLevel(tierLevel);
            
            // Cập nhật subscription expiry
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(durationDays);
            employer.setSubscriptionExpiresAt(expiresAt);
            
            employerRepository.save(employer);

            response.put("success", true);
            response.put("message", "Đã cập nhật tier thành công");
            response.put("data", Map.of(
                "userId", userId,
                "tierLevel", tierLevel,
                "tierName", getTierName(tierLevel),
                "expiresAt", expiresAt.toString()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API để xem tier hiện tại của user
     * GET /api/admin/payment/tier/{userId}
     */
    @GetMapping("/tier/{userId}")
    public ResponseEntity<Map<String, Object>> getUserTier(@PathVariable Integer userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Employer> employerOpt = employerRepository.findByUserId(userId);
            
            if (employerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy employer profile cho userId: " + userId);
                return ResponseEntity.notFound().build();
            }

            Employer employer = employerOpt.get();
            Integer tierLevel = employer.getTierLevel() != null ? employer.getTierLevel() : 0;
            
            boolean isActive = employer.getSubscriptionExpiresAt() != null && 
                             employer.getSubscriptionExpiresAt().isAfter(LocalDateTime.now());

            response.put("success", true);
            response.put("data", Map.of(
                "userId", userId,
                "tierLevel", tierLevel,
                "tierName", getTierName(tierLevel),
                "subscriptionExpiresAt", employer.getSubscriptionExpiresAt() != null ? 
                    employer.getSubscriptionExpiresAt().toString() : "null",
                "isActive", isActive
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API để reset về Free tier
     * POST /api/admin/payment/reset-to-free/{userId}
     */
    @PostMapping("/reset-to-free/{userId}")
    public ResponseEntity<Map<String, Object>> resetToFree(@PathVariable Integer userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("tierLevel", 0);
        request.put("durationDays", 365);
        
        return resetUserTier(request);
    }

    private String getTierName(Integer tierLevel) {
        return switch (tierLevel) {
            case 0 -> "Free";
            case 1 -> "Basic";
            case 2 -> "Premium";
            case 3 -> "Enterprise";
            default -> "Unknown";
        };
    }
}
