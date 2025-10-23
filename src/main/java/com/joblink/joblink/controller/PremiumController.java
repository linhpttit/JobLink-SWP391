package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.model.*;
import com.joblink.joblink.service.PaymentService;
import com.joblink.joblink.service.PremiumService;
import com.joblink.joblink.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobseeker/premium")
public class PremiumController {
    private final PremiumService premiumService;
    private final PaymentService paymentService;
    private final ProfileService profileService;

    public PremiumController(PremiumService premiumService, PaymentService paymentService,
                             ProfileService profileService) {
        this.premiumService = premiumService;
        this.paymentService = paymentService;
        this.profileService = profileService;
    }

    @GetMapping
    public String showPremiumPackages(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<PremiumPackage> packages = premiumService.getJobSeekerPackages();
        PremiumSubscription activeSubscription = premiumService.getActiveSubscription(user.getUserId());

        model.addAttribute("packages", packages);
        model.addAttribute("activeSubscription", activeSubscription);
        model.addAttribute("user", user);

        return "premium-packages";
    }

    /**
     * Bấm "Mua" gói:
     *  - Tạo Invoice(PENDING) + Payment(PENDING, sinh txRef)
     *  - Redirect sang payment-demo kèm txRef
     */
    @PostMapping("/purchase/{packageId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> purchasePackage(@PathVariable int packageId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            // Đảm bảo có profile (nếu chưa có sẽ tạo rỗng)
            JobSeekerProfile profile = profileService.getOrCreateProfile(user.getUserId());

            // Tạo hóa đơn & payment (PENDING)
            Invoice invoice = paymentService.createInvoiceForPackage(user.getUserId(), null, profile.getSeekerId(), packageId);
            Payment payment = paymentService.initiatePayment(invoice.getInvoiceId(), "DEMO", "demo");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("redirectUrl",
                    "/jobseeker/premium/payment-demo?packageId=" + packageId + "&txRef=" + payment.getTxRef());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Trang demo thanh toán – PHẢI nhận txRef từ server
     */
    @GetMapping("/payment-demo")
    public String showPaymentDemo(@RequestParam int packageId,
                                  @RequestParam(required = false) String txRef,
                                  HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        PremiumPackage pkg = premiumService.getPackageById(packageId);
        if (pkg == null) {
            return "redirect:/jobseeker/premium";
        }

        try {
            // Nếu thiếu txRef (người dùng vào link cũ), tự tạo Invoice + Payment rồi redirect kèm txRef
            if (txRef == null || txRef.isBlank()) {
                JobSeekerProfile profile = profileService.getOrCreateProfile(user.getUserId());
                Invoice invoice = paymentService.createInvoiceForPackage(user.getUserId(), null, profile.getSeekerId(), packageId);
                Payment payment = paymentService.initiatePayment(invoice.getInvoiceId(), "DEMO", "demo");
                return "redirect:/jobseeker/premium/payment-demo?packageId=" + packageId + "&txRef=" + payment.getTxRef();
            }

            // Có txRef rồi thì render trang demo
            model.addAttribute("packageId", packageId);
            model.addAttribute("packageName", pkg.getName());
            model.addAttribute("amount", pkg.getPrice());
            model.addAttribute("duration", pkg.getDurationDays());
            model.addAttribute("txRef", txRef);
            model.addAttribute("user", user);

            return "payment-demo";
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Có lỗi xảy ra: " + e.getMessage());
            return "payment-result";
        }
    }


    /**
     * Callback mô phỏng kết quả thanh toán
     */
    @GetMapping("/payment/callback")
    public String paymentCallback(@RequestParam String txRef,
                                  @RequestParam String status,
                                  @RequestParam int packageId,
                                  HttpSession session,
                                  Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            if ("SUCCESS".equalsIgnoreCase(status)) {
                // LẤY PROFILE THEO userId (đúng quan hệ) + đảm bảo tồn tại
                JobSeekerProfile profile = profileService.getOrCreateProfile(user.getUserId());
                if (profile == null) {
                    model.addAttribute("success", false);
                    model.addAttribute("message", "Bạn cần tạo hồ sơ JobSeeker trước khi mua gói Premium.");
                    return "payment-result";
                }

                // Cập nhật payment/invoice, kích hoạt gói
                paymentService.processPaymentSuccess(
                        txRef,
                        packageId,
                        user.getUserId(),   // userId
                        null,               // employerId (để null nếu không dùng)
                        profile.getSeekerId()
                );

                model.addAttribute("success", true);
                model.addAttribute("message", "Thanh toán thành công! Gói Premium của bạn đã được kích hoạt.");
            } else {
                model.addAttribute("success", false);
                model.addAttribute("message", "Thanh toán thất bại. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "payment-result";
    }

    @GetMapping("/subscription")
    @ResponseBody
    public ResponseEntity<PremiumSubscription> getActiveSubscription(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        PremiumSubscription subscription = premiumService.getActiveSubscription(user.getUserId());
        if (subscription == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/invoices")
    public String showInvoices(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Invoice> invoices = paymentService.getUserInvoices(user.getUserId());
        model.addAttribute("invoices", invoices);
        model.addAttribute("user", user);

        return "premium-invoices";
    }
}
