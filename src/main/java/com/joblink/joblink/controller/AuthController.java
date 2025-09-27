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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Email: " + form.getEmail());
        System.out.println("Remember parameter: " + remember);

        User u = auth.authenticate(form.getEmail(), form.getPassword());
        if (u == null) {
            System.out.println("Authentication failed for: " + form.getEmail());
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            model.addAttribute("loginRequest", form);
            return "signin";
        }

        System.out.println("Authentication successful for: " + u.getEmail());
        System.out.println("User ID: " + u.getUserId());
        System.out.println("User Role: " + u.getRole());

        session.setAttribute("user", u);

        // Xử lý Remember Me - chấp nhận cả "on" và null check
        if (remember != null && ("on".equals(remember) || "true".equals(remember))) {
            System.out.println("Setting remember cookie for user ID: " + u.getUserId());
            rememberMeService.remember(resp, u.getUserId());
        } else {
            System.out.println("Clearing remember cookie");
            rememberMeService.clear(resp);
        }

        String redirectUrl = switch (u.getRole().toLowerCase()) {
            case "admin"    -> "redirect:/admin/home";
            case "employer" -> "redirect:/employer/home";
            case "seeker"   -> "redirect:/seeker/home";
            default         -> "redirect:/signin"; // Fixed: was "/login"
        };

        System.out.println("Redirecting to: " + redirectUrl);
        return redirectUrl;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse resp) {
        System.out.println("=== LOGOUT DEBUG ===");
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                System.out.println("Logging out user: " + user.getEmail());
            }
            session.invalidate();
        }
        rememberMeService.clear(resp);
        System.out.println("Redirecting to signin page");
        return "redirect:/signin"; // Fixed: was "/login"
    }

    /* =========================
       REGISTER
       ========================= */

    @GetMapping("/signup")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String doRegister(@ModelAttribute RegisterRequest form, Model model) {
        try {
            // Validation cho các trường bắt buộc
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

            // Kiểm tra độ mạnh của mật khẩu
            if (!form.getPassword().matches(PASSWORD_POLICY_REGEX)) {
                throw new IllegalArgumentException("Mật khẩu phải ≥8 ký tự và chứa ít nhất 1 chữ hoa, 1 số, 1 ký tự đặc biệt");
            }

            // Đăng ký người dùng
            auth.register(form.getEmail(), form.getPassword(), form.getRole());

            model.addAttribute("msg", "Đăng ký thành công. Hãy đăng nhập.");
            model.addAttribute("loginRequest", new LoginRequest());
            return "signin";

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

    /* =========================
       FORGOT PASSWORD
       ========================= */

    @GetMapping("/forgot")
    public String forgotPage() {
        return "forgot";
    }

    @PostMapping("/forgot")
    public String doReset(@RequestParam String email,
                          @RequestParam String newPassword,
                          @RequestParam String confirmPassword,
                          Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "forgot";
        }

        if (!newPassword.matches(PASSWORD_POLICY_REGEX)) {
            model.addAttribute("error",
                    "Mật khẩu phải ≥8 ký tự và chứa ít nhất 1 chữ hoa, 1 số, 1 ký tự đặc biệt");
            return "forgot";
        }

        int updated = userDao.resetPassword(email, newPassword);
        if (updated == 0) {
            model.addAttribute("error", "Email không tồn tại");
            return "forgot";
        }

        model.addAttribute("msg", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập.");
        return "forgot";
    }
}