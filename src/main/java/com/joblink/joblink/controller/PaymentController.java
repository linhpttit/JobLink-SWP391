package com.joblink.joblink.controller;

// --- Repository Imports ---
import com.joblink.joblink.Repository.*;

// --- Entity Imports ---
import com.joblink.joblink.entity.*;

// --- DTO and Service Imports ---
import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.service.PayOSService;
import com.joblink.joblink.service.SubscriptionService;

// --- Spring and Java Imports ---
import jakarta.servlet.http.HttpServletResponse; // Import HttpServletResponse for redirect
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// --- PayOS SDK Imports ---
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.io.IOException; // Import IOException
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());

    // --- Injected Dependencies ---
    private final PayOSService payOSService;
    private final UserRepository userRepository;
    private final PremiumPackageRepository packageRepository;
    private final JobSeekerProfileRepository seekerRepository;
    private final SubscriptionRepository subscriptionsRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;

    private static final String SESSION_USER_KEY = "user";

    private UserSessionDTO getJobseekerSession(HttpSession session) {
        return (UserSessionDTO) session.getAttribute(SESSION_USER_KEY);
    }

    // --- Show Packages Page (Unchanged) ---
    @GetMapping("/packages")
    public String showPackagesPage(HttpSession session, Model model) {
        // ... (Keep the existing logic) ...
        try {
            UserSessionDTO user = getJobseekerSession(session);
            if (user == null) return "redirect:/signin";
            JobSeekerProfile seeker = seekerRepository.findByUserId(user.getUserId()).orElse(null);
            List<PremiumPackages> packages = packageRepository.findByUserTypeAndIsActiveTrueOrderByPriceAsc("JOBSEEKER");
            model.addAttribute("packages", packages);
            PremiumSubscriptions currentSub = null;
            if (seeker != null) currentSub = subscriptionService.getCurrentActiveSubscription(seeker.getSeekerId());
            model.addAttribute("currentSubscription", currentSub);
            return "packages";
        } catch (Exception e) {
          model.addAttribute("error", "Cannot load package page.");
            return "error";
        }
    }

    // --- Create PayOS Order & Redirect Endpoint ---
    @PostMapping("/create")
    @Transactional
    public String createPayOSOrderAndRedirect(
            @RequestParam Integer packageId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            HttpServletResponse httpServletResponse) { // Inject HttpServletResponse

        UserSessionDTO user = getJobseekerSession(session);
        if (user == null) return "redirect:/signin";

        try {
            // 1. Get necessary info
            JobSeekerProfile seeker = seekerRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new NoSuchElementException("Seeker profile not found."));
            PremiumPackages pkg = packageRepository.findById(packageId)
                    .orElseThrow(() -> new NoSuchElementException("Package ID not found: " + packageId));

            // 2. Create local DB records (Invoice, Subscription, Payment) in PENDING state
            Invoice invoice = Invoice.builder()
                    .userId(user.getUserId()).seekerId(seeker.getSeekerId())
                    .amount(pkg.getPrice()).status("PENDING")
                    .issuedAt(LocalDateTime.now()).dueAt(LocalDateTime.now().plusMinutes(15))
                    .build();
            Invoice savedInvoice = invoiceRepository.save(invoice);
            String internalOrderId = String.valueOf(savedInvoice.getInvoiceId());

            PremiumSubscriptions subscription = new PremiumSubscriptions();
            // ... (set subscription fields as before) ...
            subscription.setUserId(user.getUserId()); subscription.setSeekerId(seeker.getSeekerId());
            subscription.setPackageId(pkg.getPackageId()); subscription.setStatus("PENDING");
            subscription.setIsActive(false); subscription.setCreatedAt(LocalDateTime.now());
            subscription.setInvoiceId(savedInvoice.getInvoiceId());
            subscription.setStartDate(null); subscription.setEndDate(null);
            PremiumSubscriptions savedSub = subscriptionsRepository.save(subscription);

            savedInvoice.setSubscriptionId(savedSub.getSubscriptionId());
            invoiceRepository.save(savedInvoice);

            // 3. Call PayOS Service
            CreatePaymentLinkResponse payosResponse = payOSService.createPaymentLink(internalOrderId, pkg, user);
            String checkoutUrl = payosResponse.getCheckoutUrl(); // Get the PayOS checkout URL
            Long payosOrderCode = payosResponse.getOrderCode(); // Get the PayOS code

            // 4. Create local Payment record
            Payment payment = new Payment();
            payment.setInvoiceId(savedInvoice.getInvoiceId());
            payment.setProvider("PAYOS");
            payment.setTxRef(internalOrderId);
            // No need to save payosOrderCode in DB if not changing schema
            payment.setAmount(pkg.getPrice());
            payment.setStatus("PENDING");
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // --- 5. IMMEDIATE REDIRECT TO PAYOS ---
            if (checkoutUrl == null || checkoutUrl.trim().isEmpty()) {
                logger.severe("PayOS did not return a checkoutUrl for PayOS Code: " + payosOrderCode);
                throw new Exception("Could not get PayOS checkout link.");
            }

            logger.info("Redirecting user to PayOS checkout URL: " + checkoutUrl + " for Internal ID: " + internalOrderId);
            httpServletResponse.setHeader("Location", checkoutUrl);
            httpServletResponse.setStatus(HttpServletResponse.SC_FOUND); // 302 Redirect
            return null; // Return null because the response is handled directly

        } catch (NoSuchElementException e) {
            logger.warning("Error creating PayOS order (Not Found): " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/payment/packages"; // Redirect back with error
        } catch (Exception e) {
          redirectAttributes.addFlashAttribute("error", "System error creating PayOS payment. Please try again.");
            return "redirect:/payment/packages"; // Redirect back with error
        }
    }

    // --- REMOVE QR Code Page Endpoint ---
    // @GetMapping("/qr-code")
    // public String showQrCodePage(...) { ... } // DELETE THIS METHOD

    // --- REMOVE Check Status Endpoint (Polling) ---
    // @GetMapping("/qr-status/{internalOrderId}")
    // public ResponseEntity<?> checkStatusRoute(...) { ... } // DELETE THIS METHOD

    // --- REMOVE Cancel Endpoint (related to QR page) ---
    // @PostMapping("/cancel/{internalOrderId}")
    // public ResponseEntity<?> cancelPayOSPaymentRoute(...) { ... } // DELETE THIS METHOD
    // Note: Users will cancel on the PayOS page itself. Webhook should handle cancellation status updates.

    // --- Success Page (Callback/Redirect from PayOS) ---
    // This endpoint REMAINS ESSENTIAL
    @GetMapping("/success")
    @Transactional // Still needed if webhook is delayed and this needs to activate
    public String paymentSuccess(
            @RequestParam String orderId, // This is internalOrderId (invoiceId as String)
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        logger.info("[Payment Success Callback] Received for Internal ID: " + orderId);
        UserSessionDTO user = getJobseekerSession(session);
        if (user == null) return "redirect:/signin";

        try {
            Payment payment = paymentRepository.findByTxRef(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Payment info not found on callback (Internal ID: " + orderId + ")"));

            String currentStatus = payment.getStatus();
            logger.info("[Payment Success Callback] DB Status for Internal ID " + orderId + ": " + currentStatus);

            // Check DB status (expecting webhook to have updated it ideally)
            if ("PAID".equals(currentStatus) || "SUCCESS".equals(currentStatus)) {
                model.addAttribute("message", "Payment successful! Your premium package has been activated.");
            } else if ("PENDING".equals(currentStatus)) {
                // Webhook might be delayed. Activate here as a fallback.
                logger.warning("[Payment Success Callback] Status still PENDING for " + orderId + ". Attempting activation...");
                try {
                    subscriptionService.activateSubscription(payment);
                    logger.info("[Payment Success Callback] Subscription activated for Internal ID: " + orderId);
                    model.addAttribute("message", "Payment successful! Your premium package is now active.");
                } catch (Exception activationError) {
                   model.addAttribute("message", "Payment confirmed, but there was an issue activating the package immediately. Please check your dashboard or contact support.");
                    model.addAttribute("isProcessing", true); // Indicate potential delay
                }
            } else { // CANCELLED, EXPIRED, FAILED...
                redirectAttributes.addFlashAttribute("error", "The transaction was not successful or was cancelled/expired (Status: " + currentStatus + ")");
                return "redirect:/payment/packages";
            }

            Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
            model.addAttribute("invoice", invoice);
            model.addAttribute("payment", payment);

            return "success"; // success.html template

        } catch (NoSuchElementException e) {
            logger.warning("[Payment Success Callback] Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Could not find the corresponding transaction information.");
            return "redirect:/payment/packages";
        } catch (Exception e) {
           redirectAttributes.addFlashAttribute("error", "System error confirming payment.");
            return "redirect:/payment/packages";
        }
    }

    // --- View Invoice Page (Unchanged) ---
    @GetMapping("/invoice")
    public String viewInvoice(@RequestParam String orderId, Model model, HttpSession session) {
        UserSessionDTO userSession = getJobseekerSession(session);
        if (userSession == null) return "redirect:/signin";
        try {
            Integer invoiceId = Integer.parseInt(orderId); // Giả sử orderId là invoiceId
            // Lấy Invoice (và kiểm tra quyền sở hữu)
            Invoice invoice = invoiceRepository.findByInvoiceIdAndUserId(invoiceId, userSession.getUserId())
                    .orElseThrow(() -> new NoSuchElementException("Invoice ID: " + orderId + " not found for this user."));

            // Lấy User liên quan
            User user = userRepository.findById(invoice.getUserId())
                    .orElse(null); // Tìm User bằng userId từ Invoice

            // Lấy Subscription liên quan (nếu có)
            PremiumSubscriptions subscription = null;
            if (invoice.getSubscriptionId() != null) {
                subscription = subscriptionsRepository.findById(invoice.getSubscriptionId()).orElse(null);
            }

            // Lấy Package liên quan (nếu có Subscription)
            PremiumPackages premiumPackage = null;
            if (subscription != null) {
                premiumPackage = packageRepository.findById(subscription.getPackageId()).orElse(null);
            }

            // Đưa tất cả vào Model
            model.addAttribute("invoice", invoice);
            model.addAttribute("customer", user); // Dùng tên "customer"
            model.addAttribute("subscription", subscription);
            model.addAttribute("premiumPackage", premiumPackage); // Dùng tên "premiumPackage"

            return "invoice"; // Template path

        } catch (NumberFormatException | NoSuchElementException e) {
            logger.warning("Error viewing invoice: " + e.getMessage());
            model.addAttribute("error", "Mã hóa đơn không hợp lệ hoặc không tồn tại.");
            return "error";
        } catch (Exception e) {
          model.addAttribute("error", "Lỗi hệ thống khi tải hóa đơn.");
            return "error";
        }
    }
}
