package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.dao.UserDao;
import com.joblink.joblink.dto.LoginRequest;
import com.joblink.joblink.dto.RegisterRequest;
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

    @GetMapping("/api/session-check")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Boolean> res = new HashMap<>();
        res.put("loggedIn", user != null);
        return ResponseEntity.ok(res);
    }
    @GetMapping("/login")
    public String redirectLogin() {
        return "redirect:/signin";
    }

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

        // ✅ Lưu user vào session
        session.setAttribute("user", u);

        // ✅ Ghi / xoá cookie REMEMBER
        if ("on".equalsIgnoreCase(remember) || "true".equalsIgnoreCase(remember)) {
            rememberMeService.remember(resp, u.getUserId());
        } else {
            rememberMeService.clear(resp);
        }

        // ✅ Chuyển hướng đúng role
        String redirectUrl = switch (u.getRole().toLowerCase()) {
            case "admin" -> "redirect:/admin/home";
            case "employer" -> "redirect:/employer/home";
            case "seeker"   -> "redirect:/seeker/home";
            default -> "redirect:/";
        };

        System.out.println("[AuthController] ✅ Login success: " + u.getEmail() + " → " + redirectUrl);
        return redirectUrl;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse resp) {
        try {
            if (session != null) {
                session.invalidate();
                System.out.println("[AuthController] 🧹 Session invalidated");
            }
        } catch (Exception ignored) {}

        // ✅ Xoá cookie REMEMBER
        rememberMeService.clear(resp);
        System.out.println("[AuthController] 🧹 REMEMBER cookie cleared");
        return "redirect:/signin";
    }

    /* ======================================================
       ✅ 3. Register + OTP
       ====================================================== */
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
            validateRegisterForm(form);
            auth.startRegister(form.getEmail(), form.getPassword(), form.getRole(), session);
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

    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session, Model model, RedirectAttributes ra) {
        String email = (String) session.getAttribute("pendingEmail");
        if (email == null) {
            ra.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/signup";
        }
        model.addAttribute("email", email);
        return "Verify-OTP";
    }

    @PostMapping("/verify-otp")
    public String doVerifyOtp(@RequestParam("otpCode") String otpCode,
                              HttpSession session,
                              Model model,
                              RedirectAttributes ra) {
        try {
            if (otpCode == null || otpCode.trim().isEmpty())
                throw new IllegalArgumentException("Vui lòng nhập mã OTP");

            auth.verifyOtp(otpCode, session);

            String email = (String) session.getAttribute("pendingEmail");
            String password = (String) session.getAttribute("pendingPassword");
            User user = auth.authenticate(email, password);

            if (user != null) {
                session.setAttribute("user", user);
                clearOtpSession(session);
                return switch (user.getRole().toLowerCase()) {
                    case "admin" -> "redirect:/admin/home";
                    case "employer" -> "redirect:/employer/home";
                    case "seeker" -> "redirect:/seeker/home";
                    default -> "redirect:/";
                };
            }

            ra.addFlashAttribute("msg", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "redirect:/signin";

        } catch (IllegalArgumentException | IllegalStateException ex) {
            String email = (String) session.getAttribute("pendingEmail");
            if (email == null) {
                ra.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
                return "redirect:/signup";
            }
            model.addAttribute("email", email);
            model.addAttribute("error", ex.getMessage());
            return "Verify-OTP";
        }
    }

    @PostMapping("/resend-otp")
    @ResponseBody
    public ResponseEntity<Map<String, String>> resendOtp(HttpSession session) {
        Map<String, String> res = new HashMap<>();
        try {
            String email = (String) session.getAttribute("pendingEmail");
            String password = (String) session.getAttribute("pendingPassword");
            String role = (String) session.getAttribute("pendingRole");
            if (email == null || password == null || role == null) {
                res.put("error", "Không có phiên đăng ký. Vui lòng đăng ký lại.");
                return ResponseEntity.badRequest().body(res);
            }
            auth.startRegister(email, password, role, session);
            res.put("message", "Mã OTP mới đã được gửi đến email của bạn");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
    @GetMapping("/forgot")
    public String forgotPasswordPage() {
        return "forgot-email";
    }
    private void validateRegisterForm(RegisterRequest form) {
        if (form.getFullName() == null || form.getFullName().trim().isEmpty())
            throw new IllegalArgumentException("Họ và tên là bắt buộc");
        if (form.getUsername() == null || form.getUsername().trim().isEmpty())
            throw new IllegalArgumentException("Tên đăng nhập là bắt buộc");
        if (form.getEmail() == null || form.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Email là bắt buộc");
        if (form.getPassword() == null || form.getPassword().trim().isEmpty())
            throw new IllegalArgumentException("Mật khẩu là bắt buộc");
        if (form.getConfirmPassword() == null || !form.getPassword().equals(form.getConfirmPassword()))
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        if (form.getRole() == null || form.getRole().trim().isEmpty())
            throw new IllegalArgumentException("Vai trò là bắt buộc");
        if (!form.isAgreeTerms())
            throw new IllegalArgumentException("Bạn phải đồng ý với điều khoản dịch vụ");
        if (!form.getPassword().matches(PASSWORD_POLICY_REGEX))
            throw new IllegalArgumentException("Mật khẩu phải ≥8 ký tự và chứa ít nhất 1 chữ hoa, 1 số, 1 ký tự đặc biệt");
    }

    private void clearOtpSession(HttpSession session) {
        session.removeAttribute("pendingEmail");
        session.removeAttribute("pendingPassword");
        session.removeAttribute("pendingRole");
        session.removeAttribute("otp");
        session.removeAttribute("otpCreatedAt");
    }
}
