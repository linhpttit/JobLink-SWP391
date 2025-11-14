package com.joblink.joblink.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VNPayConfig {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.api-url}")
    private String apiUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;

    @Value("${vnpay.order-type}")
    private String orderType;

    /**
     * Log VNPay configuration khi application start
     */
    @PostConstruct
    public void logConfig() {
        System.out.println("\n========================================");
        System.out.println("📌 VNPAY CONFIGURATION LOADED:");
        System.out.println("========================================");
        System.out.println("✅ TMN Code: " + (tmnCode != null && !tmnCode.isEmpty() ? tmnCode : "❌ MISSING"));
        System.out.println("✅ Hash Secret: " + (hashSecret != null && !hashSecret.isEmpty() ? maskSecret(hashSecret) : "❌ MISSING"));
        System.out.println("✅ API URL: " + apiUrl);
        System.out.println("✅ Return URL: " + returnUrl);
        System.out.println("✅ Version: " + version);
        System.out.println("✅ Command: " + command);
        System.out.println("✅ Order Type: " + orderType);
        
        // Validation warnings
        if (tmnCode == null || tmnCode.isEmpty() || "YOUR_TMN_CODE".equals(tmnCode)) {
            System.out.println("\n⚠️ WARNING: TMN Code chưa được cập nhật!");
        }
        if (hashSecret == null || hashSecret.isEmpty() || "YOUR_HASH_SECRET".equals(hashSecret)) {
            System.out.println("⚠️ WARNING: Hash Secret chưa được cập nhật!");
        }
        
        boolean isConfigured = tmnCode != null && !tmnCode.isEmpty() && 
                              hashSecret != null && !hashSecret.isEmpty() &&
                              !"YOUR_TMN_CODE".equals(tmnCode) &&
                              !"YOUR_HASH_SECRET".equals(hashSecret);
        
        if (isConfigured) {
            System.out.println("\n✅ VNPay configuration is READY!");
        } else {
            System.out.println("\n❌ VNPay configuration is NOT ready. Please update credentials.");
        }
        System.out.println("========================================\n");
    }
    
    /**
     * Mask secret for security (show first 4 and last 4 characters)
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.length() < 8) {
            return "***";
        }
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }
}
