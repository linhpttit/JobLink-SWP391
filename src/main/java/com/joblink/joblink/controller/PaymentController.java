package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.entity.PaymentTransaction;
import com.joblink.joblink.entity.SubscriptionPackage;
import com.joblink.joblink.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Trang hiển thị các gói nâng cấp (upgrade account)
     */
    @GetMapping("/upgrade")
    public String showUpgradePage(HttpSession session, Model model) {
        // Kiểm tra user đã login chưa
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Kiểm tra user có phải là employer không
        if (!"employer".equalsIgnoreCase(user.getRole())) {
            model.addAttribute("error", "Chỉ có nhà tuyển dụng mới có thể nâng cấp tài khoản");
            return "error";
        }

        // Lấy danh sách các gói subscription
        List<SubscriptionPackage> packages = paymentService.getAllActivePackages();
        model.addAttribute("packages", packages);

        // Lấy thông tin tier hiện tại
        Map<String, Object> tierInfo = paymentService.getEmployerTierInfo(user.getUserId());
        model.addAttribute("currentTier", tierInfo.get("tierLevel"));
        model.addAttribute("subscriptionExpiresAt", tierInfo.get("subscriptionExpiresAt"));
        model.addAttribute("isSubscriptionActive", tierInfo.get("isSubscriptionActive"));
        model.addAttribute("user", user);

        return "payment/upgrade";
    }

    /**
     * Tạo payment URL và redirect đến VNPay
     * Hoặc activate trực tiếp nếu là gói Free (price = 0)
     */
    @PostMapping("/create")
    public String createPayment(
            @RequestParam("packageId") Long packageId,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        try {
<<<<<<< HEAD
            // Kiểm tra user đã login chưa
            UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
            if (user == null) {
                return "redirect:/auth/login";
=======
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


LocalDateTime start = LocalDateTime.now();
LocalDateTime end = start.plusDays(pkg.getDurationDays());
            PremiumSubscriptions subscription = new PremiumSubscriptions();
            // ... (set subscription fields as before) ...
            subscription.setUserId(user.getUserId()); subscription.setSeekerId(seeker.getSeekerId());
            subscription.setPackageId(pkg.getPackageId()); subscription.setStatus("PENDING");
            subscription.setIsActive(false); subscription.setCreatedAt(LocalDateTime.now());
            subscription.setInvoiceId(savedInvoice.getInvoiceId());
            subscription.setStartDate(start);
            subscription.setEndDate(end);
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
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141
            }

            // Lấy thông tin package
            SubscriptionPackage selectedPackage = paymentService.getPackageById(packageId);
            if (selectedPackage == null) {
                redirectAttributes.addFlashAttribute("error", "Gói không tồn tại");
                return "redirect:/payment/upgrade";
            }

            // Kiểm tra tier hiện tại
            Map<String, Object> tierInfo = paymentService.getEmployerTierInfo(user.getUserId());
            Integer currentTier = (Integer) tierInfo.get("tierLevel");
            Boolean isActive = (Boolean) tierInfo.get("isSubscriptionActive");

            // Không cho phép mua gói thấp hơn tier hiện tại (nếu đang active)
            if (isActive && selectedPackage.getTierLevel() < currentTier) {
                String currentTierName = currentTier == 0 ? "Free" : 
                                        (currentTier == 1 ? "Basic" : 
                                        (currentTier == 2 ? "Premium" : "Enterprise"));
                redirectAttributes.addFlashAttribute("error", 
                    "Bạn đang sử dụng gói " + currentTierName + ". Không thể hạ cấp xuống gói thấp hơn.");
                return "redirect:/payment/upgrade";
            }

            // Không cho phép mua lại gói hiện tại (nếu đang active)
            if (isActive && selectedPackage.getTierLevel().equals(currentTier)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Bạn đang sử dụng gói này. Vui lòng chọn gói cao hơn để nâng cấp.");
                return "redirect:/payment/upgrade";
            }

            // Nếu là gói Free (price = 0), activate trực tiếp
            if (selectedPackage.getPrice() == 0) {
                boolean success = paymentService.activateFreePackage(user.getUserId(), packageId);
                if (success) {
                    redirectAttributes.addFlashAttribute("success", 
                        "Chúc mừng! Bạn đã kích hoạt gói " + selectedPackage.getPackageName() + " thành công!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Không thể kích hoạt gói Free. Vui lòng thử lại.");
                }
                return "redirect:/payment/upgrade";
            }

            // Lấy IP address
            String ipAddress = getClientIp(request);

            // Tạo payment URL cho gói trả phí
            String paymentUrl = paymentService.createPaymentUrl(user.getUserId(), packageId, ipAddress);

            // Redirect đến VNPay
            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo thanh toán: " + e.getMessage());
            return "redirect:/payment/upgrade";
        }
    }

    /**
     * Xử lý callback từ VNPay
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpSession session,
            Model model) {
        
        // Xử lý kết quả thanh toán
        Map<String, Object> result = paymentService.processPaymentReturn(params);

        model.addAttribute("success", result.get("success"));
        model.addAttribute("message", result.get("message"));
        
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        }

        return "payment/payment-result";
    }


    /**
     * Trang lịch sử thanh toán
     */
    @GetMapping("/history")
    public String showPaymentHistory(HttpSession session, Model model) {
        // Kiểm tra user đã login chưa
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Chỉ employer mới xem được lịch sử thanh toán
        if (!"employer".equalsIgnoreCase(user.getRole())) {
            model.addAttribute("error", "Chỉ có nhà tuyển dụng mới có thể xem lịch sử thanh toán");
            return "error";
        }

        // Lấy lịch sử thanh toán
        List<PaymentTransaction> transactions = paymentService.getPaymentHistory(user.getUserId());
        model.addAttribute("transactions", transactions);
        model.addAttribute("user", user);

        // Lấy thông tin tier hiện tại
        Map<String, Object> tierInfo = paymentService.getEmployerTierInfo(user.getUserId());
        model.addAttribute("currentTier", tierInfo.get("tierLevel"));
        model.addAttribute("subscriptionExpiresAt", tierInfo.get("subscriptionExpiresAt"));
        model.addAttribute("isSubscriptionActive", tierInfo.get("isSubscriptionActive"));

        // Tính toán thống kê
        long successCount = transactions.stream()
                .filter(t -> "SUCCESS".equals(t.getPaymentStatus()))
                .count();
        long totalSpent = transactions.stream()
                .filter(t -> "SUCCESS".equals(t.getPaymentStatus()))
                .mapToLong(PaymentTransaction::getAmount)
                .sum();
        
        model.addAttribute("successCount", successCount);
        model.addAttribute("totalSpent", totalSpent);

        return "payment/history";
    }

    /**
     * Lấy client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
