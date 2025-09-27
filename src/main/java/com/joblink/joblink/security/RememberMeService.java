package com.joblink.joblink.security;


import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.dao.UserDao;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Ghi cookie REMEMBER và xác thực lại bằng HMAC
 */
@Component
public class RememberMeService {

    public static final String COOKIE_NAME = "REMEMBER";

    @Value("${app.remember.secret:ChangeThisSecretString}")
    private String secret;

    @Value("${app.remember.days:14}")
    private int days;

    private final UserDao userDao;

    public RememberMeService(UserDao userDao) {
        this.userDao = userDao;
    }

    /** Ghi cookie remember cho userId */
    public void remember(HttpServletResponse resp, int userId) {
        long exp = System.currentTimeMillis() / 1000 + days * 24L * 3600L;
        String data = userId + ":" + exp;
        String sig = hmac(secret, data);
        String token = data + ":" + sig;

        Cookie ck = new Cookie(COOKIE_NAME, token);
        ck.setPath("/");
        ck.setHttpOnly(true);
        // ck.setSecure(true); // bật khi HTTPS
        ck.setMaxAge((int) (days * 24L * 3600L));
        resp.addCookie(ck);
    }

    /** Xoá cookie remember */
    public void clear(HttpServletResponse resp) {
        Cookie ck = new Cookie(COOKIE_NAME, "");
        ck.setPath("/");
        ck.setMaxAge(0);
        resp.addCookie(ck);
    }

    /** Đọc cookie và trả về User nếu hợp lệ */
    public User autoLogin(HttpServletRequest req) {
        String token = getCookie(req);
        if (token == null) {
            System.out.println("No REMEMBER cookie found");
            return null;
        }

        System.out.println("Found REMEMBER token: " + token);

        String[] parts = token.split(":");
        if (parts.length != 3) {
            System.out.println("Invalid token format");
            return null;
        }

        try {
            int userId = Integer.parseInt(parts[0]);
            long exp = Long.parseLong(parts[1]);

            System.out.println("Token userId: " + userId + ", exp: " + exp);

            if (System.currentTimeMillis() / 1000 > exp) {
                System.out.println("Token expired");
                return null;
            }

            String data = parts[0] + ":" + parts[1];
            String sig = hmac(secret, data);
            if (!sig.equals(parts[2])) {
                System.out.println("Invalid signature");
                return null;
            }

            User user = userDao.findById(userId);
            System.out.println("Found user: " + (user != null ? user.getEmail() : "null"));
            return user;
        } catch (Exception e) {
            System.out.println("Error parsing token: " + e.getMessage());
            return null;
        }
    }

    /** Chỉ lấy userId từ cookie */
    public Integer getUserIdFromCookie(HttpServletRequest req) {
        String token = getCookie(req);
        if (token == null) return null;
        String[] parts = token.split(":");
        if (parts.length != 3) return null;
        try {
            int userId = Integer.parseInt(parts[0]);
            long exp = Long.parseLong(parts[1]);
            if (System.currentTimeMillis() / 1000 > exp) return null;
            String data = parts[0] + ":" + parts[1];
            String sig = hmac(secret, data);
            if (!sig.equals(parts[2])) return null;
            return userId;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (COOKIE_NAME.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private String hmac(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

