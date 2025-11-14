package com.joblink.joblink.controller;

import com.joblink.joblink.config.VNPayConfig;
import com.joblink.joblink.util.VNPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Debug controller để test hash calculation
 */
@RestController
@RequestMapping("/debug/hash")
public class VNPayDebugController {

    @Autowired
    private VNPayConfig vnPayConfig;

    /**
     * Test hash với demo data từ VNPay
     */
    @GetMapping("/test")
    public Map<String, Object> testHash() {
        Map<String, Object> response = new LinkedHashMap<>();
        
        try {
            // Sample data giống demo
            Map<String, String> vnpParams = new LinkedHashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", "10000000");
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", "TEST12345678");
            vnpParams.put("vnp_OrderInfo", "Thanh toan don hang:TEST12345678");
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", "127.0.0.1");
            vnpParams.put("vnp_CreateDate", "20250111120000");
            vnpParams.put("vnp_ExpireDate", "20250111121500");
            
            // Sort keys
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            
            // Build hash data - MANUAL (giống demo)
            StringBuilder hashDataManual = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashDataManual.append(fieldName);  // Key KHÔNG encode
                    hashDataManual.append('=');
                    hashDataManual.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));  // Value encode
                    if (itr.hasNext()) {
                        hashDataManual.append('&');
                    }
                }
            }
            
            // Build hash data - VIA UTIL
            String hashDataUtil = VNPayUtil.buildHashData(vnpParams);
            
            // Calculate hash
            String hashManual = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashDataManual.toString());
            String hashUtil = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashDataUtil);
            
            // Build query string
            StringBuilder queryManual = new StringBuilder();
            itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    queryManual.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    queryManual.append('=');
                    queryManual.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        queryManual.append('&');
                    }
                }
            }
            
            String queryUtil = VNPayUtil.buildQueryString(vnpParams);
            
            // Final URLs
            String urlManual = vnPayConfig.getApiUrl() + "?" + queryManual.toString() + "&vnp_SecureHash=" + hashManual;
            String urlUtil = vnPayConfig.getApiUrl() + "?" + queryUtil + "&vnp_SecureHash=" + hashUtil;
            
            response.put("config", Map.of(
                "tmnCode", vnPayConfig.getTmnCode(),
                "hashSecret", maskSecret(vnPayConfig.getHashSecret()),
                "apiUrl", vnPayConfig.getApiUrl()
            ));
            
            response.put("sortedParams", fieldNames);
            
            response.put("hashData", Map.of(
                "manual", hashDataManual.toString(),
                "util", hashDataUtil,
                "match", hashDataManual.toString().equals(hashDataUtil) ? "✅ MATCH" : "❌ DIFFERENT"
            ));
            
            response.put("hash", Map.of(
                "manual", hashManual,
                "util", hashUtil,
                "length", hashManual.length(),
                "match", hashManual.equals(hashUtil) ? "✅ MATCH" : "❌ DIFFERENT"
            ));
            
            response.put("queryString", Map.of(
                "manual", queryManual.toString(),
                "util", queryUtil,
                "match", queryManual.toString().equals(queryUtil) ? "✅ MATCH" : "❌ DIFFERENT"
            ));
            
            response.put("finalUrl", Map.of(
                "manual", urlManual,
                "util", urlUtil,
                "match", urlManual.equals(urlUtil) ? "✅ MATCH" : "❌ DIFFERENT"
            ));
            
            // Test encoding của một string cụ thể
            String testString = "Thanh toan don hang:TEST12345678";
            String encoded = URLEncoder.encode(testString, StandardCharsets.US_ASCII.toString());
            response.put("testEncoding", Map.of(
                "original", testString,
                "encoded", encoded,
                "expectedInHash", "vnp_OrderInfo=Thanh+toan+don+hang%3ATEST12345678"
            ));
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("stackTrace", Arrays.toString(e.getStackTrace()));
        }
        
        return response;
    }
    
    /**
     * Test với real payment data
     */
    @GetMapping("/test-real")
    public Map<String, Object> testRealPayment() {
        Map<String, Object> response = new LinkedHashMap<>();
        
        try {
            // Tạo params giống như trong PaymentService
            String txnRef = "TXN" + System.currentTimeMillis() + VNPayUtil.getRandomNumber(4);
            
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVersion());
            vnpParams.put("vnp_Command", vnPayConfig.getCommand());
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", "49900000"); // 499,000 * 100
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", "Thanh toan goi Premium");
            vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", "127.0.0.1");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String createDate = LocalDateTime.now().format(formatter);
            String expireDate = LocalDateTime.now().plusMinutes(15).format(formatter);
            vnpParams.put("vnp_CreateDate", createDate);
            vnpParams.put("vnp_ExpireDate", expireDate);
            
            // Build hash data
            String hashData = VNPayUtil.buildHashData(vnpParams);
            String secureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
            
            // Build query string
            String queryUrl = VNPayUtil.buildQueryString(vnpParams);
            queryUrl += "&vnp_SecureHash=" + secureHash;
            
            String fullUrl = vnPayConfig.getApiUrl() + "?" + queryUrl;
            
            response.put("params", vnpParams);
            response.put("hashData", hashData);
            response.put("secureHash", secureHash);
            response.put("secureHashLength", secureHash.length());
            response.put("queryString", queryUrl);
            response.put("fullUrl", fullUrl);
            
            // Validation
            Map<String, String> validation = new LinkedHashMap<>();
            validation.put("1_tmnCode", vnPayConfig.getTmnCode() + " (length: " + vnPayConfig.getTmnCode().length() + ")");
            validation.put("2_hashSecret", maskSecret(vnPayConfig.getHashSecret()) + " (length: " + vnPayConfig.getHashSecret().length() + ")");
            validation.put("3_hashLength", secureHash.length() + " chars " + (secureHash.length() == 128 ? "✅" : "❌ Should be 128"));
            validation.put("4_paramsCount", vnpParams.size() + " params");
            
            response.put("validation", validation);
            
            // Show hash secret bytes for debugging
            byte[] secretBytes = vnPayConfig.getHashSecret().getBytes(StandardCharsets.UTF_8);
            response.put("secretBytesLength", secretBytes.length);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Compare current implementation vs demo exactly
     */
    @GetMapping("/compare-with-demo")
    public Map<String, Object> compareWithDemo() {
        Map<String, Object> response = new LinkedHashMap<>();
        
        try {
            // Exact same params as demo
            Map<String, String> params = new LinkedHashMap<>();
            params.put("vnp_Amount", "1806000");
            params.put("vnp_Command", "pay");
            params.put("vnp_CreateDate", "20210801153333");
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_IpAddr", "127.0.0.1");
            params.put("vnp_Locale", "vn");
            params.put("vnp_OrderInfo", "Thanh toan don hang :5");
            params.put("vnp_OrderType", "other");
            params.put("vnp_ReturnUrl", "https://domainmerchant.vn/ReturnUrl");
            params.put("vnp_TmnCode", "DEMOV210");
            params.put("vnp_TxnRef", "5");
            params.put("vnp_Version", "2.1.0");
            
            String demoHashSecret = "RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ";
            String expectedHash = "3e0d61a0c0534b2e36680b3f7277743e8784cc4e1d68fa7d276e79c23be7d6318d338b477910a27992f5057bb1582bd44bd82ae8009ffaf6d141219218625c42";
            
            // Calculate hash
            String hashData = VNPayUtil.buildHashData(params);
            String calculatedHash = VNPayUtil.hmacSHA512(demoHashSecret, hashData);
            
            response.put("demoParams", params);
            response.put("demoHashSecret", maskSecret(demoHashSecret));
            response.put("hashData", hashData);
            response.put("expectedHash", expectedHash);
            response.put("calculatedHash", calculatedHash);
            response.put("match", calculatedHash.equals(expectedHash) ? "✅ PERFECT MATCH!" : "❌ DIFFERENT");
            
            if (!calculatedHash.equals(expectedHash)) {
                response.put("note", "Hash không khớp với demo - có thể là lỗi trong cách tính hash");
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    private String maskSecret(String secret) {
        if (secret == null || secret.length() < 8) return "***";
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }
}
