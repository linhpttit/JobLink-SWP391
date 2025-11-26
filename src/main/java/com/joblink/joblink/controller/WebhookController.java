package com.joblink.joblink.controller;

import com.joblink.joblink.repository.PaymentRepository;
import com.joblink.joblink.dto.ApiResponse;
import com.joblink.joblink.entity.Payment;
import com.joblink.joblink.service.PayOSService; // Import PayOSService for helper method
import com.joblink.joblink.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

import java.util.logging.Logger;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private static final Logger logger = Logger.getLogger(WebhookController.class.getName());

    private final PayOS payOS;
    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;

    @PostMapping("/payos")
    public ResponseEntity<ApiResponse<String>> handlePayOSWebhook(@RequestBody Object body) {
        try {
            logger.info("Received PayOS Webhook...");
            // Log raw body for debugging if needed
            // logger.info("Webhook Body Raw: " + body.toString());

            // Verify and parse data
            WebhookData data = payOS.webhooks().verify(body);
            if (data == null) {
                logger.severe("✗ Webhook data parsing failed.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Invalid webhook data"));
            }

            Long payosOrderCode = data.getOrderCode();
            String description = data.getDescription();
            String webhookStatusCode = data.getCode(); // "00" for success
            String webhookDesc = data.getDesc();

            logger.info("✓ Webhook parsed. PayOS Code: " + payosOrderCode + ", Status Code: " + webhookStatusCode + ", Desc: " + webhookDesc);
            logger.info("Webhook Description: " + description);

            // *** Extract Internal Order ID from Description ***
            String internalOrderId = PayOSService.extractInternalOrderId(description); // Use static helper

            if (internalOrderId == null) {
                logger.warning("✗ Could not extract Internal Order ID from description: '" + description + "'. Ignoring webhook.");
                // Return OK so PayOS doesn't retry
                return ResponseEntity.ok(ApiResponse.success("Webhook ignored: Cannot find internal ID.", null));
            }
            logger.info("Extracted Internal Order ID: " + internalOrderId);

            // Find Payment record using internalOrderId (txRef)
            Payment payment = paymentRepository.findByTxRef(internalOrderId).orElse(null);

            if (payment == null) {
                logger.warning("✗ Payment record not found for Internal Order ID: " + internalOrderId + " (from webhook). Ignoring.");
                return ResponseEntity.ok(ApiResponse.success("Webhook ignored: Payment not found.", null));
            }

            logger.info("Found Payment record (ID: " + payment.getPaymentId() + ", Status: " + payment.getStatus() + ") for Internal ID: " + internalOrderId);

            // Process only if current status is PENDING
            if ("PENDING".equals(payment.getStatus())) {
                if ("00".equals(webhookStatusCode)) { // Success code
                    logger.info("Webhook indicates SUCCESS. Activating subscription for Internal ID: " + internalOrderId);
                    try {
                        subscriptionService.activateSubscription(payment);
                        logger.info("✓ Subscription activated via webhook for Internal ID: " + internalOrderId);
                        return ResponseEntity.ok(ApiResponse.success("Webhook processed: Subscription activated.", webhookStatusCode));
                    } catch (Exception activationError) {
                      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error during activation: " + activationError.getMessage()));
                    }
                } else { // Non-success codes
                    logger.warning("Webhook indicates non-success (" + webhookStatusCode + ": " + webhookDesc + ") for Internal ID: " + internalOrderId);
                    String newStatus = "FAILED"; // Default
                    if ("99".equals(webhookStatusCode)) newStatus = "CANCELLED"; // Assume 99 is cancel
                    // Map other PayOS codes to your statuses (e.g., EXPIRED) if needed
                    payment.setStatus(newStatus);
                    paymentRepository.save(payment);
                    // Update related Invoice/Subscription if needed
                    subscriptionService.updateSubscriptionStatus(payment.getInvoiceId(), newStatus); // Add this method to SubscriptionService
                    logger.info("✓ Updated payment status to " + newStatus + " via webhook for Internal ID: " + internalOrderId);
                    return ResponseEntity.ok(ApiResponse.success("Webhook processed: Status updated.", webhookStatusCode));
                }
            } else {
                logger.info("Payment status already '" + payment.getStatus() + "' for Internal ID: " + internalOrderId + ". Ignoring webhook.");
                return ResponseEntity.ok(ApiResponse.success("Webhook ignored: Already processed.", null));
            }


        } catch (IllegalArgumentException e) {
            logger.severe("✗ Webhook verification possibly failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Webhook data error")); // Changed from 401
        } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Webhook processing error"));
        }
    }
}