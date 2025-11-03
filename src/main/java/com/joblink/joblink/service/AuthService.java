package com.joblink.joblink.service;

import java.util.Locale;
import java.util.Optional;
import java.util.Random;

import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.auth.util.PasswordPolicy;
import com.joblink.joblink.dao.UserDao;
import com.joblink.joblink.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

    private final UserDao userDao;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserDao userDao, EmailService emailService,UserRepository userRepository,
    		PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* =========================================================
       1) ĐĂNG KÝ TRỰC TIẾP (chỉ dùng nội bộ cho OTP step 2)
       ========================================================= */
    public void register(String email, String password, String role) {
        final String normEmail = normalizeEmail(email);
        final String r = normalizeRole(role);

        if (!isAllowedRole(r)) {
            throw new IllegalArgumentException("Role chỉ được admin, employer hoặc seeker");
        }
        if (!PasswordPolicy.isValid(password)) {
            throw new IllegalArgumentException("Mật khẩu phải ≥8 ký tự, chứa ít nhất 1 chữ hoa, 1 số và 1 ký tự đặc biệt");
        }
        if (userDao.emailExists(normEmail)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        try {
            userDao.register(normEmail, password, r);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Không thể tạo tài khoản: " + ex.getMostSpecificCause().getMessage());
        }
    }

    public void startRegister(String email, String password, String role, HttpSession session) {
        final String normEmail = normalizeEmail(email);
        final String r = normalizeRole(role);

        // Validate sớm (nhưng CHƯA ghi DB)
        if (!isAllowedRole(r)) {
            throw new IllegalArgumentException("Role chỉ được admin, employer hoặc seeker");
        }
        if (!PasswordPolicy.isValid(password)) {
            throw new IllegalArgumentException("Mật khẩu phải ≥8 ký tự, có ít nhất 1 chữ hoa, 1 số, 1 ký tự đặc biệt");
        }
        if (userDao.emailExists(normEmail)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        String otp = generateOtp();
        emailService.sendOtp(normEmail, otp);

        // Lưu phiên chờ xác minh
        session.setAttribute("pendingEmail", normEmail);
        session.setAttribute("pendingPassword", password);
        session.setAttribute("pendingRole", r);
        session.setAttribute("otp", otp);
        session.setAttribute("otpCreatedAt", System.currentTimeMillis());

        System.out.println("[AuthService] OTP sent to: " + normEmail);
    }

    public void verifyOtp(String inputOtp, HttpSession session) {
        String otp       = (String) session.getAttribute("otp");
        String email     = (String) session.getAttribute("pendingEmail");
        String password  = (String) session.getAttribute("pendingPassword");
        String role      = (String) session.getAttribute("pendingRole");
        Long createdAt   = (Long) session.getAttribute("otpCreatedAt");

        if (otp == null || email == null || password == null || role == null) {
            throw new IllegalStateException("Không có phiên đăng ký đang chờ xác minh");
        }

        if (createdAt == null || System.currentTimeMillis() - createdAt > 5 * 60 * 1000) {
            clearOtpSession(session);
            throw new IllegalArgumentException("OTP đã hết hạn, vui lòng đăng ký lại");
        }

        if (!otp.equals(inputOtp == null ? "" : inputOtp.trim())) {
            throw new IllegalArgumentException("Mã OTP không đúng");
        }

        register(email, password, role);
        clearOtpSession(session);
        System.out.println("[AuthService] Register success for: " + email);
    }

    public void sendOtpForPasswordReset(String email, HttpSession session) {
        final String normEmail = normalizeEmail(email);

        if (normEmail == null || normEmail.isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }

        User user = userDao.findByEmail(normEmail);
        if (user == null) {
            throw new IllegalArgumentException("Email không tồn tại trong hệ thống");
        }

        String otp = generateOtp();
        emailService.sendOtp(normEmail, otp);

        session.setAttribute("otp", otp);
        session.setAttribute("otpCreatedAt", System.currentTimeMillis());

        System.out.println("[AuthService] Reset OTP sent to: " + normEmail);
    }

    public void verifyOtpForPasswordReset(String inputOtp, HttpSession session) {
        String storedOtp  = (String) session.getAttribute("otp");
        Long otpCreatedAt = (Long) session.getAttribute("otpCreatedAt");

        if (storedOtp == null || otpCreatedAt == null) {
            throw new IllegalStateException("Không tìm thấy mã OTP. Vui lòng yêu cầu gửi lại.");
        }

        if (System.currentTimeMillis() - otpCreatedAt > 5 * 60 * 1000) {
            clearOtpSession(session);
            throw new IllegalStateException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại.");
        }

        if (!storedOtp.equals(inputOtp == null ? "" : inputOtp.trim())) {
            throw new IllegalArgumentException("Mã OTP không đúng. Vui lòng thử lại.");
        }
    }

    public com.joblink.joblink.entity.User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        try {
            final String normEmail = normalizeEmail(email);
//            User u = userDao.login(normEmail, password.trim());
            com.joblink.joblink.entity.User u = userRepository.findByEmail(normEmail).get();
            if (u == null) {
                System.out.println("[AuthService] Login failed for: " + normEmail);
            } else if(!passwordEncoder.matches(password, u.getPasswordHash())) {
            	System.out.println("[AuthService] Login failed for: " + normEmail);
            	u = null;
            } 
            return u;
        } catch (Exception e) {
            System.out.println("[AuthService] Login error: " + e.getMessage());
            return null;
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedRole(String role) {
        return "admin".equals(role) || "employer".equals(role) || "seeker".equals(role);
    }

    private String generateOtp() {
        int otp = 100000 + new Random().nextInt(900000);
        return String.valueOf(otp);
    }

    private void clearOtpSession(HttpSession session) {
        session.removeAttribute("otp");
        session.removeAttribute("otpCreatedAt");
        session.removeAttribute("pendingEmail");
        session.removeAttribute("pendingPassword");
        session.removeAttribute("pendingRole");
    }
}
