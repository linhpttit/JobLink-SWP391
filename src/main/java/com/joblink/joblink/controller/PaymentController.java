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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
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
     */
    @PostMapping("/create")
    public String createPayment(
            @RequestParam("packageId") Long packageId,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Kiểm tra user đã login chưa
            UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
            if (user == null) {
                return "redirect:/auth/login";
            }

            // Lấy IP address
            String ipAddress = getClientIp(request);

            // Tạo payment URL
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
