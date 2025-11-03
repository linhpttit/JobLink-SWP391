package com.joblink.joblink.service;

import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;


import com.joblink.joblink.entity.PremiumPackages;
import com.joblink.joblink.dto.UserSessionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

@Service
public class PayOSService {

    private static final Logger logger = Logger.getLogger(PayOSService.class.getName());

    @Autowired
    private PayOS payOS;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${application.base-url:http://localhost}")
    private String baseUrl;

    // Format for including internal ID in description
    private static final String DESC_PREFIX = "INV"; // Keep it short

    /**
     * Creates a PayOS payment link.
     * @param internalOrderId Your system's internal order ID (e.g., invoiceId as String)
     * @param pkg The premium package being purchased.
     * @param user The current user session.
     * @return The response from the PayOS API.
     */
    public CreatePaymentLinkResponse createPaymentLink(String internalOrderId, PremiumPackages pkg, UserSessionDTO user) throws Exception {

        if (pkg == null) throw new IllegalArgumentException("Package information invalid");
        if (user == null || user.getUserId() == null) throw new IllegalArgumentException("User information invalid");
        if (internalOrderId == null || internalOrderId.trim().isEmpty()) throw new IllegalArgumentException("Internal Order ID invalid");

        if (pkg.getPrice() == null || pkg.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Package price invalid: " + pkg.getPrice());
        }
        long amount = pkg.getPrice().longValue();

        long payosOrderCode = new Date().getTime() / 1000L + user.getUserId(); // Unique code for PayOS
        logger.info("[PayOS] Generating PayOS Order Code: " + payosOrderCode);

        String localBaseUrl = baseUrl + (serverPort.equals("80") || serverPort.equals("443") ? "" : ":" + serverPort);
        String returnUrl = localBaseUrl + "/payment/success?method=payos&orderId=" + internalOrderId; // Use internal ID in callback
        String cancelUrl = localBaseUrl + "/payment/packages";
        logger.info("[PayOS] Return URL: " + returnUrl);
        logger.info("[PayOS] Cancel URL: " + cancelUrl);

        List<PaymentLinkItem> items = Collections.singletonList(
                new PaymentLinkItem(pkg.getName(), 1, amount, null, null)
        );

        // *** IMPORTANT: Include internalOrderId in description ***
        String description = DESC_PREFIX + internalOrderId + " " + pkg.getCode(); // e.g., "INV123 SEEKERPRO"
        if (description.length() > 50) { // PayOS v2 limit
            description = description.substring(0, 50);
        }
        logger.info("[PayOS] Description: " + description);

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(payosOrderCode)
                .amount(amount)
                .description(description) // Description now contains internal ID
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .items(items)
                .expiredAt(System.currentTimeMillis() / 1000L + 900) // 15 mins expiry
                .build();

        try {
            logger.info("[PayOS] Sending create request for PayOS Code: " + payosOrderCode + " (Internal ID: " + internalOrderId + ")");
            CreatePaymentLinkResponse result = payOS.paymentRequests().create(paymentData);

            if (result == null) throw new Exception("PayOS API returned null response");
            if (result.getOrderCode() == null || !result.getOrderCode().equals(payosOrderCode)) {
                logger.severe("[PayOS] ✗ Critical Error: PayOS order code mismatch. Expected: " + payosOrderCode + ", Got: " + result.getOrderCode());
                throw new Exception("PayOS order code mismatch.");
            }
            if ((result.getQrCode() == null || result.getQrCode().trim().isEmpty()) && (result.getCheckoutUrl() == null || result.getCheckoutUrl().trim().isEmpty()) ) {
                logger.warning("[PayOS] No QR code or Checkout URL returned.");
            }

            logger.info("[PayOS] ✓ Create successful for PayOS Code: " + result.getOrderCode());
            return result;

        } catch (Exception e) {
            logger.severe("[PayOS] ✗ Error creating link for PayOS Code " + payosOrderCode + ": " + e.getMessage());
            if (e instanceof vn.payos.exception.PayOSException pe) logger.severe("[PayOS] API Error: " + pe.getCause() + " - " + pe.getMessage());
            throw new Exception("Error creating PayOS payment link: " + e.getMessage(), e);
        }
    }

    /**
     * Gets PayOS payment status using the PayOS orderCode.
     */
    public PaymentLink getPaymentStatus(Long payosOrderCode) throws Exception {
        if (payosOrderCode == null || payosOrderCode <= 0) throw new IllegalArgumentException("Invalid PayOS Order Code");
        try {
            logger.info("[PayOS] Checking status for PayOS Code: " + payosOrderCode);
            PaymentLink result = payOS.paymentRequests().get(payosOrderCode);
            logger.info("[PayOS] Status result for " + payosOrderCode + ": " + (result != null ? result.getStatus().name() : "null"));
            return result; // Can be null if not found by PayOS API
        } catch (Exception e) {
            logger.severe("[PayOS] ✗ Error checking status for PayOS Code " + payosOrderCode + ": " + e.getMessage());
            if (e instanceof vn.payos.exception.PayOSException pe && "NOT_FOUND".equals(pe.getCause())) return null; // Treat API NOT_FOUND as null
            throw new Exception("Error checking PayOS status: " + e.getMessage(), e);
        }
    }

    /**
     * Cancels PayOS payment link using the PayOS orderCode.
     */
    public PaymentLink cancelPayment(Long payosOrderCode) throws Exception {
        if (payosOrderCode == null || payosOrderCode <= 0) throw new IllegalArgumentException("Invalid PayOS Order Code");
        String reason = "User cancelled";
        try {
            logger.info("[PayOS] Sending cancel for PayOS Code: " + payosOrderCode + " Reason: " + reason);
            PaymentLink result = payOS.paymentRequests().cancel(payosOrderCode, reason);
            if (result == null) throw new Exception("PayOS API returned null on cancel");
            logger.info("[PayOS] ✓ Cancel successful for PayOS Code " + payosOrderCode + ", new status: " + result.getStatus().name());
            return result;
        } catch (Exception e) {
            logger.severe("[PayOS] ✗ Error cancelling PayOS Code " + payosOrderCode + ": " + e.getMessage());
            if (e instanceof vn.payos.exception.PayOSException pe) {
                logger.severe("[PayOS] Cancel API Error: " + pe.getCause() + " - " + pe.getMessage());
                if ("NOT_FOUND".equals(pe.getCause())) throw new NoSuchElementException("PayOS Order Code " + payosOrderCode + " not found for cancellation.");
                else throw new Exception("PayOS API error (" + pe.getCause() + "): " + pe.getMessage(), e);
            }
            throw new Exception("System error during PayOS cancellation: " + e.getMessage(), e);
        }
    }

    /**
     * Helper to extract Internal Order ID from Description.
     * Assumes format "PREFIX<InternalID> ..." e.g., "INV123 ..."
     * @param description The description string from WebhookData or PaymentLink.
     * @return The extracted Internal Order ID or null if not found/invalid.
     */
    public static String extractInternalOrderId(String description) {
        if (description == null || !description.startsWith(DESC_PREFIX)) {
            return null;
        }
        try {
            String potentiallyIdPart = description.substring(DESC_PREFIX.length());
            // Find the first space after the prefix
            int spaceIndex = potentiallyIdPart.indexOf(' ');
            String idPart = (spaceIndex == -1) ? potentiallyIdPart : potentiallyIdPart.substring(0, spaceIndex);
            // Optional: Add validation if your internal ID always follows a pattern (e.g., only digits)
            // if (!idPart.matches("\\d+")) return null;
            return idPart.trim();
        } catch (Exception e) {
            logger.warning("Error parsing internal order ID from description '" + description + "': " + e.getMessage());
            return null;
        }
    }
}