package com.joblink.joblink.security;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.dao.UserDao;
import com.joblink.joblink.entity.JobSeekerProfile;
import com.joblink.joblink.repository.JobSeekerProfileRepository;
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
import java.util.Optional;

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
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    
    public RememberMeService(JdbcTemplate jdbc, UserDao userDao, 
                             JobSeekerProfileRepository jobSeekerProfileRepository) {
        this.jdbc = jdbc;
        this.userDao = userDao;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
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
            if (u == null) {
                System.out.println("[RememberMe] ⚠️ User not found for cookie userId=" + userId);
                return null;
            }

            // ✅ Kiểm tra nếu job seeker bị khóa (soft delete) thì không cho auto-login
            String role = u.getRole() != null ? u.getRole().toLowerCase() : "";
            if ("seeker".equals(role)) {
                if (isJobSeekerLocked((int) userId)) {
                    clear(res);
                    System.out.println("[RememberMe] ❌ Auto-login blocked: Job seeker với user_id=" + userId + " đã bị khóa");
                    return null;
                }
            }

            System.out.println("[RememberMe] ✅ Auto-login success for userId=" + userId);
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

    /**
     * Kiểm tra xem job seeker có bị khóa (soft delete) không
     * @param userId ID của user
     * @return true nếu bị khóa, false nếu không
     */
    private boolean isJobSeekerLocked(Integer userId) {
        try {
            if (userId == null) {
                return false;
            }
            
            Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findByUserId(userId);
            if (profileOpt.isEmpty()) {
                // Chưa có profile, cho phép auto-login (có thể tạo profile sau)
                return false;
            }
            
            JobSeekerProfile profile = profileOpt.get();
            // Nếu isLocked = true, thì bị khóa
            return profile.getIsLocked() != null && profile.getIsLocked();
        } catch (Exception e) {
            System.err.println("[RememberMe] Lỗi khi kiểm tra trạng thái job seeker: " + e.getMessage());
            // Nếu có lỗi, không chặn auto-login (fail-open)
            return false;
        }
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
