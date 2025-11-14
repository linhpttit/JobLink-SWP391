package com.joblink.joblink.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtil {

    /**
     * Generate HMAC SHA512 signature - THEO VNPAY CONFIG.JAVA
     * 
     * QUAN TRỌNG: key.getBytes() KHÔNG chỉ định charset!
     * VNPay Demo:
     * byte[] hmacKeyBytes = key.getBytes();  // <- NO CHARSET!
     * byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
     */
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();  // <- QUAN TRỌNG: KHÔNG chỉ định charset!
            SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            
            // Convert to hex
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
            
        } catch (Exception ex) {
            throw new RuntimeException("Error generating HMAC SHA512", ex);
        }
    }


    /**
     * Build query string from parameters - THEO JAVA DEMO
     * Query string: Encode CẢ KEY VÀ VALUE
     */
    public static String buildQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        // Sort parameters by key
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build query string - ENCODE CẢ KEY VÀ VALUE
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                }
            }
        }
        return query.toString();
    }

    /**
     * Build hash data for signature - THEO JAVA DEMO
     * Hash data: KEY KHÔNG encode, chỉ encode VALUE
     * 
     * Java Demo:
     * hashData.append(fieldName);  // <- KHÔNG encode
     * hashData.append('=');
     * hashData.append(URLEncoder.encode(fieldValue, US_ASCII));  // <- Encode value
     */
    public static String buildHashData(Map<String, String> params) {
        // Sort parameters by key
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    // Build hash data - KEY không encode, VALUE encode
                    hashData.append(fieldName);  // <- KHÔNG encode key
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));  // <- Encode value
                    
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Error encoding hash data", e);
                }
            }
        }
        return hashData.toString();
    }

    /**
     * Generate random transaction reference
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
