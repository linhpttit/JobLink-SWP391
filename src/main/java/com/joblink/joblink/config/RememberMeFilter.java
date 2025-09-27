package com.joblink.joblink.config;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.security.RememberMeService;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Nếu chưa có session user thì kiểm tra cookie REMEMBER để auto-login
 */
@Component
@Order(-100)
public class RememberMeFilter implements Filter {

    private final RememberMeService rememberMeService;

    public RememberMeFilter(RememberMeService rememberMeService) {
        this.rememberMeService = rememberMeService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);

        System.out.println("RememberMeFilter is running for: " + req.getRequestURI());

        if (session == null || session.getAttribute("user") == null) {
            System.out.println("No session found, checking remember me cookie...");

            // Kiểm tra cookie có tồn tại không
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("REMEMBER".equals(cookie.getName())) {
                        System.out.println("Found REMEMBER cookie: " + cookie.getValue());
                        break;
                    }
                }
            } else {
                System.out.println("No cookies found in request");
            }

            User u = rememberMeService.autoLogin(req);
            if (u != null) {
                System.out.println("Auto login successful for user: " + u.getEmail());
                req.getSession(true).setAttribute("user", u);
            } else {
                System.out.println("Auto login failed - no valid cookie or user not found");
            }
        } else {
            System.out.println("User already logged in via session");
        }

        chain.doFilter(request, response);
    }
}
