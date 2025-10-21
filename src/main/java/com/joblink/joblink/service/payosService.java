package com.joblink.joblink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class payosService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${payos.base.url}")
    private String payosBaseUrl;

    @Value("${payos.client.id}")
    private String payosClientId;

    @Value("${payos.api.key}")
    private String payosApiKey;

    @Value("${payos.checksum.key}")
    private String payosChecksumKey;

    @Value("${payos.returnUrl}")
    private String payosReturnUrl;

    @Value("${payos.webhookUrl}")
    private String payosWebhookUrl;


    /**
     * Tạo liên kết thanh toán PayOS (Payment Link/QR)
     * @param amount Số tiền (VNĐ)
     * @param orderCode Mã đơn hàng nội bộ (txnRef)
     * @param orderInfo Mô tả đơn hàng
     * @return Map chứa link thanh toán và mã QR
     */
    public Map<String, String> createPaymentLink(long amount, String orderCode, String orderInfo) throws Exception {

        // Số đơn hàng được sinh ngẫu nhiên từ 10000 -> 99999
        long payosOrderCode = System.currentTimeMillis() % 100000;
        if (payosOrderCode < 10000) payosOrderCode += 10000;

        // 1. Chuẩn bị Request Body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderCode", payosOrderCode); // Mã đơn hàng PayOS
        requestBody.put("amount", amount);
        requestBody.put("description", orderInfo);
        requestBody.put("items", new Object[]{}); // Items list, có thể bỏ trống

        // Custom Data để PayOS trả về cùng với Webhook/Callback (Chứa TxnRef nội bộ)
        requestBody.put("cancelUrl", payosReturnUrl);
        requestBody.put("returnUrl", payosReturnUrl);
        requestBody.put("buyerName", orderCode); // Dùng buyerName để mang txnRef nội bộ

        // 2. Chuyển Body thành JSON String để tính Checksum
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 3. Tính Checksum
        String signature = hmacSHA256(jsonBody, payosChecksumKey);

        // 4. Chuẩn bị Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-client-id", payosClientId);
        headers.add("x-api-key", payosApiKey);
        headers.add("signature", signature);

        // 5. Gọi API PayOS
        String url = payosBaseUrl;

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                JsonNode.class);

        JsonNode responseNode = responseEntity.getBody();

        if (responseNode != null && responseNode.path("code").asText().equals("00")) {
            JsonNode data = responseNode.path("data");

            Map<String, String> result = new HashMap<>();
            result.put("status", "SUCCESS");
            result.put("paymentUrl", data.path("checkoutUrl").asText()); // Link thanh toán
            result.put("qrCode", data.path("qrCode").asText()); // Base64 QR code
            result.put("payosOrderCode", String.valueOf(payosOrderCode));
            result.put("txnRef", orderCode); // Mã nội bộ của mình

            return result;
        } else {
            String errorMessage = responseNode != null ? responseNode.path("desc").asText() : "Lỗi không xác định PayOS";
            throw new RuntimeException("Lỗi PayOS: " + errorMessage);
        }
    }


    /**
     * Xác minh chữ ký (Checksum) từ PayOS Callback/Webhook
     * PayOS dùng HMAC SHA256.
     * @param data JSON body hoặc Query String Map (Callback)
     * @param receivedSignature Chữ ký nhận được từ PayOS
     * @return True nếu hợp lệ
     */
    public boolean verifySignature(Map<String, String> data, String receivedSignature) throws Exception {

        // 1. Sắp xếp params theo thứ tự alphabet
        String queryString = data.entrySet().stream()
                .filter(e -> !e.getKey().equals("signature"))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        // 2. Tính lại chữ ký
        String calculatedSignature = hmacSHA256(queryString, payosChecksumKey);

        // 3. So sánh
        return calculatedSignature.equals(receivedSignature);
    }

    // Helper method: Tính HMAC SHA256
    private String hmacSHA256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}