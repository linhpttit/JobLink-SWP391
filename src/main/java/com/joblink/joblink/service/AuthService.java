package com.joblink.joblink.service;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.auth.util.PasswordPolicy;
import com.joblink.joblink.dao.UserDao;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.Random;

@Service
public class AuthService {
    private final UserDao userDao;
    private final EmailService emailService;

    public AuthService(UserDao userDao, EmailService emailService) {
        this.userDao = userDao;
        this.emailService = emailService;
    }

    /**
     * Hàm cũ – ghi user thẳng xuống DB
     */
    public void register(String email, String password, String role) {
        // Validate role
        String r = role == null ? "" : role.toLowerCase();
        if (!r.equals("employer") && !r.equals("seeker")) {
            throw new IllegalArgumentException("Role chỉ được employer hoặc seeker");
        }
        // Validate password theo policy
        if (!PasswordPolicy.isValid(password)) {
            throw new IllegalArgumentException("Mật khẩu phải >=8 ký tự, có ít nhất 1 chữ hoa, 1 số, 1 ký tự đặc biệt");
        }
        // Validate email
        if (userDao.emailExists(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        // Insert DB
        try {
            userDao.register(email, password, r);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Bước 1: Đăng ký tạm -> gửi OTP
     */
    public void startRegister(String email, String password, String role, HttpSession session) {
        // Validate sớm bằng hàm cũ, nhưng chưa insert DB
        String r = role == null ? "" : role.toLowerCase();
        if (!r.equals("employer") && !r.equals("seeker")) {
            throw new IllegalArgumentException("Role chỉ được employer hoặc seeker");
        }
        if (!PasswordPolicy.isValid(password)) {
            throw new IllegalArgumentException("Mật khẩu phải >=8 ký tự, có ít nhất 1 chữ hoa, 1 số, 1 ký tự đặc biệt");
        }
        if (userDao.emailExists(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Sinh OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        emailService.sendOtp(email, otp);

        // Lưu session
        session.setAttribute("pendingEmail", email);
        session.setAttribute("pendingPassword", password);
        session.setAttribute("pendingRole", r);
        session.setAttribute("otp", otp);
        session.setAttribute("otpCreatedAt", System.currentTimeMillis());
    }

    /**
     * Bước 2: Xác minh OTP -> nếu đúng thì gọi register()
     */
    public void verifyOtp(String inputOtp, HttpSession session) {
        String otp = (String) session.getAttribute("otp");
        String email = (String) session.getAttribute("pendingEmail");
        String password = (String) session.getAttribute("pendingPassword");
        String role = (String) session.getAttribute("pendingRole");
        Long createdAt = (Long) session.getAttribute("otpCreatedAt");

        if (otp == null || email == null || password == null || role == null) {
            throw new IllegalStateException("Không có phiên đăng ký đang chờ xác minh");
        }

        if (createdAt == null || System.currentTimeMillis() - createdAt > 5 * 60 * 1000) {
            clearSession(session);
            throw new IllegalArgumentException("OTP đã hết hạn, vui lòng đăng ký lại");
        }

        if (!otp.equals(inputOtp)) {
            throw new IllegalArgumentException("OTP không đúng");
        }

        // OTP đúng -> gọi lại register()
        register(email, password, role);

        clearSession(session);
    }

    private void clearSession(HttpSession session) {
        session.removeAttribute("pendingEmail");
        session.removeAttribute("pendingPassword");
        session.removeAttribute("pendingRole");
        session.removeAttribute("otp");
        session.removeAttribute("otpCreatedAt");
    }

    public User authenticate(String email, String password) {
        return userDao.login(email, password);
    }
}
