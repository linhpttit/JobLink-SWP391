package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.dao.UserDao;
import com.joblink.joblink.dto.RegisterRequest;
import com.joblink.joblink.dto.LoginRequest;
import com.joblink.joblink.security.RememberMeService;
import com.joblink.joblink.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    private static final String PASSWORD_POLICY_REGEX =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";

    private final UserDao userDao;
    private final RememberMeService rememberMeService;
    private final AuthService auth;

    public AuthController(UserDao userDao,
                          RememberMeService rememberMeService,
                          AuthService auth) {
        this.userDao = userDao;
        this.rememberMeService = rememberMeService;
        this.auth = auth;
    }

    /* =========================
       API ENDPOINTS
       ========================= */

    @GetMapping("/api/session-check")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Boolean> response = new HashMap<>();
        response.put("loggedIn", user != null);
        return ResponseEntity.ok(response);
    }

    /* =========================
       LOGIN / LOGOUT
       ========================= */

    @GetMapping("/signin")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "signin";
    }

    @PostMapping("/login")
    public String doLogin(@ModelAttribute LoginRequest form,
                          @RequestParam(value = "remember", required = false) String remember,
                          HttpSession session,
                          HttpServletResponse resp,
                          Model model) {

        User u = auth.authenticate(form.getEmail(), form.getPassword());
        if (u == null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            model.addAttribute("loginRequest", form);
            return "signin";
        }

        session.setAttribute("user", u);

        if (remember != null && ("on".equals(remember) || "true".equals(remember))) {
            rememberMeService.remember(resp, u.getUserId());
        } else {
            rememberMeService.clear(resp);
        }

        String redirectUrl = switch (u.getRole().toLowerCase()) {
            case "admin"    -> "redirect:/admin/home";
            case "employer" -> "redirect:/employer/home";
            case "seeker"   -> "redirect:/seeker/home";
            default         -> "redirect:/signin";
        };

        return redirectUrl;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse resp) {
        if (session != null) {
            session.invalidate();
        }
        rememberMeService.clear(resp);
        return "redirect:/";
    }

    /* =========================
       REGISTER WITH OTP
       ========================= */

    @GetMapping("/signup")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String doRegister(@ModelAttribute RegisterRequest form,
                             HttpSession session,
                             Model model) {
        try {
            // Validate required fields
            validateRegisterForm(form);

            // Start OTP registration process
            auth.startRegister(form.getEmail(), form.getPassword(), form.getRole(), session);

            // Redirect to OTP verification page
            model.addAttribute("email", form.getEmail());
            return "Verify-OTP";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("registerRequest", form);
            return "signup";
        } catch (DataAccessException ex) {
            model.addAttribute("error", "Không thể đăng ký: " + ex.getMostSpecificCause().getMessage());
            model.addAttribute("registerRequest", form);
            return "signup";
        }
    }

    /**
     * GET endpoint - hiển thị trang verify OTP
     * Kiểm tra xem có session pending không
     */
    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("pendingEmail");

        // Nếu không có session pending, redirect về signup
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/signup";
        }

        model.addAttribute("email", email);
        return "Verify-OTP";
    }

    /**
     * POST endpoint - xử lý verify OTP
     * Sau khi verify thành công, tự động đăng nhập và redirect về home
     */
    @PostMapping("/verify-otp")
    public String doVerifyOtp(@RequestParam(name = "otpCode", required = false) String otpCode,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            // Validate OTP code
            if (otpCode == null || otpCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập mã OTP");
            }

            // Verify OTP and register user to database
            auth.verifyOtp(otpCode, session);

            // Get credentials from session
            String email = (String) session.getAttribute("pendingEmail");
            String password = (String) session.getAttribute("pendingPassword");

            // Auto login after successful registration
            User user = auth.authenticate(email, password);

            if (user != null) {
                session.setAttribute("user", user);

                // Clear OTP session data
                clearOtpSession(session);

                // Redirect based on role
                String redirectUrl = switch (user.getRole().toLowerCase()) {
                    case "employer" -> "redirect:/employer/home";
                    case "seeker"   -> "redirect:/seeker/home";
                    case "admin"    -> "redirect:/admin/home";
                    default         -> "redirect:/seeker/home";
                };

                return redirectUrl;
            }

            // Fallback: redirect to login (should not happen)
            redirectAttributes.addFlashAttribute("msg", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "redirect:/signin";

        } catch (IllegalArgumentException | IllegalStateException ex) {
            // Return to OTP page with error message
            String email = (String) session.getAttribute("pendingEmail");

            // If session expired, redirect to signup
            if (email == null) {
                redirectAttributes.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
                return "redirect:/signup";
            }

            model.addAttribute("email", email);
            model.addAttribute("error", ex.getMessage());
            return "Verify-OTP";
        }
    }

    /**
     * Resend OTP endpoint for registration
     * Called via AJAX from frontend
     */
    @PostMapping("/resend-otp")
    @ResponseBody
    public ResponseEntity<Map<String, String>> resendOtp(HttpSession session) {
        Map<String, String> response = new HashMap<>();

        try {
            String email = (String) session.getAttribute("pendingEmail");
            String password = (String) session.getAttribute("pendingPassword");
            String role = (String) session.getAttribute("pendingRole");

            if (email == null || password == null || role == null) {
                response.put("error", "Không có phiên đăng ký. Vui lòng đăng ký lại.");
                return ResponseEntity.badRequest().body(response);
            }

            // Resend OTP (this will generate new OTP and update session)
            auth.startRegister(email, password, role, session);

            response.put("message", "Mã OTP mới đã được gửi đến email của bạn");
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            response.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /* =========================
       FORGOT PASSWORD WITH OTP
       ========================= */

    /**
     * Step 1: Show email input page
     */
    @GetMapping("/forgot")
    public String forgotPasswordPage() {
        return "forgot-email";
    }

    /**
     * Step 2: Send OTP to email
     */
    @PostMapping("/forgot/send-otp")
    public String sendForgotPasswordOtp(@RequestParam String email,
                                        HttpSession session,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            // Check if email exists
            User user = userDao.findByEmail(email);
            if (user == null) {
                model.addAttribute("error", "Email không tồn tại trong hệ thống");
                return "forgot-email";
            }

            // Generate and send OTP (reuse the same OTP mechanism from registration)
            auth.sendOtpForPasswordReset(email, session);

            // Store email in session for forgot password flow
            session.setAttribute("forgotPasswordEmail", email);

            // Redirect to OTP verification page
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/forgot/verify-otp";

        } catch (Exception ex) {
            model.addAttribute("error", "Không thể gửi mã OTP. Vui lòng thử lại.");
            return "forgot-email";
        }
    }

    /**
     * Step 3: Show OTP verification page
     */
    @GetMapping("/forgot/verify-otp")
    public String forgotPasswordVerifyOtpPage(HttpSession session,
                                              Model model,
                                              RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("forgotPasswordEmail");

        // If no session, redirect back to forgot page
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên đã hết hạn. Vui lòng thử lại.");
            return "redirect:/forgot";
        }

        model.addAttribute("email", email);
        return "forgot-verify-otp";
    }

    /**
     * Step 4: Verify OTP
     */
    @PostMapping("/forgot/verify-otp")
    public String verifyForgotPasswordOtp(@RequestParam(name = "otpCode", required = false) String otpCode,
                                          HttpSession session,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            String email = (String) session.getAttribute("forgotPasswordEmail");

            if (email == null) {
                redirectAttributes.addFlashAttribute("error", "Phiên đã hết hạn. Vui lòng thử lại.");
                return "redirect:/forgot";
            }

            if (otpCode == null || otpCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập mã OTP");
            }

            // Verify OTP
            auth.verifyOtpForPasswordReset(otpCode, session);

            // Mark OTP as verified
            session.setAttribute("forgotPasswordOtpVerified", true);

            // Redirect to reset password page
            return "redirect:/forgot/reset-password";

        } catch (IllegalArgumentException | IllegalStateException ex) {
            String email = (String) session.getAttribute("forgotPasswordEmail");

            if (email == null) {
                redirectAttributes.addFlashAttribute("error", "Phiên đã hết hạn. Vui lòng thử lại.");
                return "redirect:/forgot";
            }

            model.addAttribute("email", email);
            model.addAttribute("error", ex.getMessage());
            return "forgot-verify-otp";
        }
    }

    /**
     * Resend OTP for forgot password
     */
    @PostMapping("/forgot/resend-otp")
    @ResponseBody
    public ResponseEntity<Map<String, String>> resendForgotPasswordOtp(HttpSession session) {
        Map<String, String> response = new HashMap<>();

        try {
            String email = (String) session.getAttribute("forgotPasswordEmail");

            if (email == null) {
                response.put("error", "Phiên đã hết hạn. Vui lòng thử lại.");
                return ResponseEntity.badRequest().body(response);
            }

            // Resend OTP
            auth.sendOtpForPasswordReset(email, session);

            response.put("message", "Mã OTP mới đã được gửi đến email của bạn");
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            response.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Step 5: Show reset password page
     */
    @GetMapping("/forgot/reset-password")
    public String resetPasswordPage(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("forgotPasswordEmail");
        Boolean otpVerified = (Boolean) session.getAttribute("forgotPasswordOtpVerified");

        // Check if OTP was verified
        if (email == null || otpVerified == null || !otpVerified) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng xác thực OTP trước khi đổi mật khẩu");
            return "redirect:/forgot";
        }

        return "forgot-reset-password";
    }

    /**
     * Step 6: Process password reset
     */
    @PostMapping("/forgot/reset-password")
    public String doResetPassword(@RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            String email = (String) session.getAttribute("forgotPasswordEmail");
            Boolean otpVerified = (Boolean) session.getAttribute("forgotPasswordOtpVerified");

            // Verify session
            if (email == null || otpVerified == null || !otpVerified) {
                redirectAttributes.addFlashAttribute("error", "Phiên đã hết hạn. Vui lòng thử lại.");
                return "redirect:/forgot";
            }

            // Validate passwords
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp");
                return "forgot-reset-password";
            }

            if (!newPassword.matches(PASSWORD_POLICY_REGEX)) {
                model.addAttribute("error",
                        "Mật khẩu phải ≥8 ký tự và chứa ít nhất 1 chữ hoa, 1 số, 1 ký tự đặc biệt");
                return "forgot-reset-password";
            }

            // Update password
            int updated = userDao.resetPassword(email, newPassword);
            if (updated == 0) {
                model.addAttribute("error", "Không thể đổi mật khẩu. Vui lòng thử lại.");
                return "forgot-reset-password";
            }

            // Clear session data
            clearForgotPasswordSession(session);

            // Redirect to login with success message
            redirectAttributes.addFlashAttribute("msg", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
            return "redirect:/signin";

        } catch (Exception ex) {
            model.addAttribute("error", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "forgot-reset-password";
        }
    }

    /* =========================
       PRIVATE HELPER METHODS
       ========================= */

    private void validateRegisterForm(RegisterRequest form) {
        if (form.getFullName() == null || form.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên là bắt buộc");
        }

        if (form.getUsername() == null || form.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập là bắt buộc");
        }

        if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email là bắt buộc");
        }

        if (form.getPassword() == null || form.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu là bắt buộc");
        }

        if (form.getConfirmPassword() == null || !form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        if (form.getRole() == null || form.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Vai trò là bắt buộc");
        }

        if (!form.isAgreeTerms()) {
            throw new IllegalArgumentException("Bạn phải đồng ý với điều khoản dịch vụ");
        }

        if (!form.getPassword().matches(PASSWORD_POLICY_REGEX)) {
            throw new IllegalArgumentException("Mật khẩu phải ≥8 ký tự và chứa ít nhất 1 chữ hoa, 1 số, 1 ký tự đặc biệt");
        }
    }

    private void clearOtpSession(HttpSession session) {
        session.removeAttribute("pendingEmail");
        session.removeAttribute("pendingPassword");
        session.removeAttribute("pendingRole");
        session.removeAttribute("otp");
        session.removeAttribute("otpCreatedAt");
    }

    private void clearForgotPasswordSession(HttpSession session) {
        session.removeAttribute("forgotPasswordEmail");
        session.removeAttribute("forgotPasswordOtpVerified");
        session.removeAttribute("otp");
        session.removeAttribute("otpCreatedAt");
    }
}