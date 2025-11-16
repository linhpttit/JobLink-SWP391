package com.joblink.joblink.security;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.dao.UserDao;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
public class RememberMeService {
    public static final String COOKIE_NAME = "REMEMBER";
    @Value("${app.remember.secret:ChangeThisSecretString}")
    private String secret;
    @Value("${app.remember.days:14}")
    private int days;
    @Value("${app.remember.refreshHoursLeft:12}")
    private int refreshHoursLeft;
    private final UserDao userDao;
    private final JdbcTemplate jdbc;
    public RememberMeService(JdbcTemplate jdbc, UserDao userDao) {
        this.jdbc = jdbc;
        this.userDao = userDao;
    }
    public void remember(HttpServletResponse resp, long userId) {
        long exp = System.currentTimeMillis() / 1000 + days * 24L * 3600L;
        String data = userId + ":" + exp;
        String sig = hmac(secret, data);
        String token = data + ":" + sig;

        writeCookie(resp, token, (int) Duration.ofDays(days).getSeconds());
        System.out.println("[RememberMe] Cookie REMEMBER created for userId=" + userId);
    }
    public void clear(HttpServletResponse resp) {
        writeCookie(resp, "", 0);
        System.out.println("[RememberMe] Cookie REMEMBER cleared");
    }
    public User autoLogin(HttpServletRequest req, HttpServletResponse res) {
        String token = getCookieValue(req);
        if (token == null || token.isBlank()) return null;

        String[] parts = token.split(":");
        if (parts.length != 3) return null;

        try {
            long userId = Long.parseLong(parts[0]);
            long exp = Long.parseLong(parts[1]);
            long now = System.currentTimeMillis() / 1000;

            if (now > exp) {
                clear(res);
                System.out.println("[RememberMe] Cookie expired for userId=" + userId);
                return null;
            }

            String sig = hmac(secret, parts[0] + ":" + parts[1]);
            if (!sig.equals(parts[2])) {
                clear(res);
                System.out.println("[RememberMe] ❌ Invalid signature in cookie!");
                return null;
            }
            if (refreshHoursLeft > 0 && (exp - now) < refreshHoursLeft * 3600L) {
                remember(res, userId);
                System.out.println("[RememberMe] ♻️ Cookie refreshed (sliding expiration)");
            }

            User u = userDao.findById((int) userId);
            if (u != null) {
                System.out.println("[RememberMe] ✅ Auto-login success for userId=" + userId);
            } else {
                System.out.println("[RememberMe] ⚠️ User not found for cookie userId=" + userId);
            }

            return u;
        } catch (Exception e) {
            System.out.println("[RememberMe] ❌ Error parsing cookie: " + e.getMessage());
            clear(res);
            return null;
        }
    }

    private void writeCookie(HttpServletResponse resp, String value, int maxAge) {
        Cookie ck = new Cookie(COOKIE_NAME, value);
        ck.setHttpOnly(true);
        ck.setPath("/");
        ck.setMaxAge(maxAge);
        resp.addCookie(ck);
    }

    private String getCookieValue(HttpServletRequest req) {
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
