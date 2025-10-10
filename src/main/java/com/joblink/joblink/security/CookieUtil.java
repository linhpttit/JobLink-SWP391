package com.joblink.joblink.security;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {
    public static void add(HttpServletResponse resp, String name, String value, int maxAgeSeconds) {
        Cookie ck = new Cookie(name, value);
        ck.setPath("/");
        ck.setHttpOnly(true);   // an toàn hơn
        // ck.setSecure(true);  // bật khi dùng https
        ck.setMaxAge(maxAgeSeconds);
        resp.addCookie(ck);
    }
    public static void delete(HttpServletResponse resp, String name) {
        Cookie ck = new Cookie(name, "");
        ck.setPath("/");
        ck.setMaxAge(0);
        resp.addCookie(ck);
    }
    public static String get(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
