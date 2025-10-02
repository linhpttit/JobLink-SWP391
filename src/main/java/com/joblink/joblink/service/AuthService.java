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
    public void sendOtpForPasswordReset(String email, HttpSession session) {
        // Generate 6-digit OTP
        String otp = generateOtp();

        // Store OTP and timestamp in session
        session.setAttribute("otp", otp);
        session.setAttribute("otpCreatedAt", System.currentTimeMillis());

        // Send OTP via email
        String subject = "JobLink - Mã xác thực đặt lại mật khẩu";
        String body = String.format(
                "Xin chào,\n\n" +
                        "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản JobLink của mình.\n\n" +
                        "Mã OTP của bạn là: %s\n\n" +
                        "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\n" +
                        "JobLink Team",
                otp
        );

        emailService.sendOtp(email, otp);
    }

    /**
     * Verify OTP for password reset
     * Similar to verifyOtp but doesn't create user account
     */
    public void verifyOtpForPasswordReset(String inputOtp, HttpSession session) {
        String storedOtp = (String) session.getAttribute("otp");
        Long otpCreatedAt = (Long) session.getAttribute("otpCreatedAt");

        // Validate OTP exists
        if (storedOtp == null || otpCreatedAt == null) {
            throw new IllegalStateException("Không tìm thấy mã OTP. Vui lòng yêu cầu gửi lại.");
        }

        // Check OTP expiration (5 minutes)
        long currentTime = System.currentTimeMillis();
        long otpAge = currentTime - otpCreatedAt;
        long fiveMinutesInMillis = 5 * 60 * 1000;

        if (otpAge > fiveMinutesInMillis) {
            throw new IllegalStateException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại.");
        }

        // Verify OTP matches
        if (!storedOtp.equals(inputOtp.trim())) {
            throw new IllegalArgumentException("Mã OTP không đúng. Vui lòng thử lại.");
        }

        // OTP is valid - no need to create account, just mark as verified
        // The controller will handle the password reset
    }

    /**
     * Generate random 6-digit OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit number
        return String.valueOf(otp);
    }
    public User authenticate(String email, String password) {
        return userDao.login(email, password);
    }
}
