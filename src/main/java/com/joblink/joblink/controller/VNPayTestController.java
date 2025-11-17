package com.joblink.joblink.controller;

import com.joblink.joblink.config.VNPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để test VNPay configuration
 * CHỈ DÙNG CHO DEVELOPMENT - XÓA TRƯỚC KHI DEPLOY PRODUCTION
 */
@RestController
@RequestMapping("/test")
public class VNPayTestController {

    @Autowired
    private VNPayConfig vnPayConfig;

    /**
     * Test endpoint để kiểm tra VNPay config
     * URL: http://localhost:8081/test/vnpay-config
     */
    @GetMapping("/vnpay-config")
    public Map<String, Object> testVNPayConfig() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Lấy các giá trị config
            String tmnCode = vnPayConfig.getTmnCode();
            String hashSecret = vnPayConfig.getHashSecret();
            String apiUrl = vnPayConfig.getApiUrl();
            String returnUrl = vnPayConfig.getReturnUrl();
            String version = vnPayConfig.getVersion();
            String command = vnPayConfig.getCommand();
            String orderType = vnPayConfig.getOrderType();
            
            // Check null values
            result.put("status", "SUCCESS");
            result.put("tmnCode", tmnCode != null ? tmnCode : "NULL");
            result.put("tmnCodeLength", tmnCode != null ? tmnCode.length() : 0);
            result.put("hashSecret", hashSecret != null ? maskSecret(hashSecret) : "NULL");
            result.put("hashSecretLength", hashSecret != null ? hashSecret.length() : 0);
            result.put("apiUrl", apiUrl);
            result.put("returnUrl", returnUrl);
            result.put("version", version);
            result.put("command", command);
            result.put("orderType", orderType);
            
            // Validation
            Map<String, String> validation = new HashMap<>();
            validation.put("tmnCodeValid", (tmnCode != null && !tmnCode.isEmpty() && !tmnCode.equals("YOUR_TMN_CODE")) ? "✅ OK" : "❌ CHƯA CẬP NHẬT");
            validation.put("hashSecretValid", (hashSecret != null && !hashSecret.isEmpty() && !hashSecret.equals("YOUR_HASH_SECRET")) ? "✅ OK" : "❌ CHƯA CẬP NHẬT");
            validation.put("apiUrlValid", (apiUrl != null && apiUrl.contains("vnpayment.vn")) ? "✅ OK" : "❌ SAI");
            validation.put("returnUrlValid", (returnUrl != null && returnUrl.contains("payment/vnpay-return")) ? "✅ OK" : "❌ SAI");
            
            result.put("validation", validation);
            
            // Overall status
            boolean allValid = validation.values().stream().allMatch(v -> v.contains("✅"));
            result.put("configStatus", allValid ? "✅ TẤT CẢ CONFIG ĐÚNG - SẴN SÀNG THANH TOÁN" : "⚠️ CÓ CONFIG CHƯA ĐÚNG - KIỂM TRA LẠI");
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("message", "Không thể đọc VNPay config. Kiểm tra file application.properties");
        }
        
        return result;
    }
    
    /**
     * Mask secret để không show full ra ngoài (security)
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.length() < 8) {
            return "***";
        }
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }
    
    /**
     * Test endpoint đơn giản
     */
    @GetMapping("/ping")
    public String ping() {
        return "✅ Server đang chạy - Port: 8081";
    }
}
