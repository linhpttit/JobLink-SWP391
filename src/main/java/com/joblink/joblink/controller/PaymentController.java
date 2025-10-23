package com.joblink.joblink.controller;


import com.joblink.joblink.dto.PaymentResponse;
import com.joblink.joblink.dto.UserSessionDTO; // <-- THÊM IMPORT NÀY
import com.joblink.joblink.entity.Invoice;
import com.joblink.joblink.entity.Payment;
import com.joblink.joblink.entity.PremiumPackages;
import com.joblink.joblink.entity.PremiumSubscriptions;
import com.joblink.joblink.entity.JobSeekerProfile;
import com.joblink.joblink.service.PaymentService;
import com.joblink.joblink.service.PremiumPackageService;
import com.joblink.joblink.service.JobSeekerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PremiumPackageService packageService;
    private final JobSeekerService seekerService;

    @GetMapping("/packages")
    public String showPackages(HttpSession session, Model model) {

        // --- SỬA LỖI Ở ĐÂY ---
        // Lấy đúng đối tượng UserSessionDTO từ session
        UserSessionDTO sessionUser = (UserSessionDTO) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login"; // Nếu chưa đăng nhập, quay về login
        }
        // Lấy userId từ đối tượng DTO
        Integer userId = sessionUser.getUserId();
        // --- KẾT THÚC SỬA LỖI ---


        JobSeekerProfile seeker = seekerService.getByUserId(userId);
        if (seeker == null) {
            model.addAttribute("error", "Không tìm thấy thông tin ứng viên");
            return "error";
        }

        List<PremiumPackages> packages = packageService.getActiveSeekerPackages();
        model.addAttribute("packages", packages);

        PremiumSubscriptions currentSub = paymentService.getCurrentActiveSubscription(seeker.getSeekerId());
        model.addAttribute("currentSubscription", currentSub);

        return "packages";
    }
    @PostMapping("/create")

    public String createPayment(
            @RequestParam Integer packageId,
            HttpSession session,
            Model model
    ) {
        // --- Lấy thông tin user ---
        UserSessionDTO sessionUser = (UserSessionDTO) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        Integer userId = sessionUser.getUserId();

        try {
            JobSeekerProfile seeker = seekerService.getByUserId(userId);
            PremiumPackages pkg = packageService.getPackageById(packageId);

            // ================================================
            // === SỬA LỖI: BẠN ĐANG THIẾU ĐOẠN KIỂM TRA NÀY ===
            // ================================================
            if (seeker == null) {
                model.addAttribute("error", "Không tìm thấy thông tin hồ sơ ứng viên (SeekerProfile) cho user ID: " + userId);
                return "error";
            }
            // ================================================

            if (pkg == null) {
                model.addAttribute("error", "Gói dịch vụ không tồn tại");
                return "error";
            }

            PaymentResponse response = paymentService.createPayment(seeker, pkg);

            model.addAttribute("orderId", response.getOrderId());
            model.addAttribute("paymentId", response.getPaymentId());
            model.addAttribute("amount", response.getAmount());
            model.addAttribute("packageName", pkg.getName());
            model.addAttribute("qrCodeUrl", response.getQrCodeUrl());

            return "qr-payment";

        } catch (Exception e) {
            // Thêm dòng này để bạn tự xem lỗi chi tiết trong console
            e.printStackTrace();

            model.addAttribute("error", "Lỗi tạo thanh toán: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/check-status/{orderId}")
    @ResponseBody
    public PaymentResponse checkPaymentStatus(@PathVariable String orderId) {
        return paymentService.checkPaymentStatus(orderId);
    }

    @PostMapping("/cancel/{orderId}")
    @ResponseBody
    public PaymentResponse cancelPayment(@PathVariable String orderId) {
        return paymentService.cancelPayment(orderId);
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String orderId, Model model) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);

        if (payment == null || !payment.getStatus().equals("SUCCESS")) {
            return "redirect:/payment/packages";
        }

        model.addAttribute("payment", payment);
        return "success";
    }

    @GetMapping("/invoice")
    public String viewInvoice(@RequestParam String orderId, Model model) {
        Invoice invoice = paymentService.getInvoiceByOrderId(orderId);

        if (invoice == null) {
            model.addAttribute("error", "Không tìm thấy hóa đơn");
            return "error";
        }

        model.addAttribute("invoice", invoice);
        return "payment/invoice";
    }
}