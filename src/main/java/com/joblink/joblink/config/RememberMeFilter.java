package com.joblink.joblink.config;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.security.RememberMeService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

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
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        // 🔹 Bỏ qua các đường dẫn không cần kiểm tra auto-login
        if (uri.startsWith("/signin") || uri.startsWith("/signup") || uri.startsWith("/login") ||
                uri.startsWith("/logout") || uri.startsWith("/forgot") || uri.startsWith("/verify-otp") ||
                uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") ||
                uri.startsWith("/static/") || uri.startsWith("/webjars/") || uri.startsWith("/api/") ||
                uri.equals("/error") || uri.equals("/404")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            System.out.println("[RememberMeFilter] No session found → checking remember cookie...");

            // ✅ Gọi autoLogin với cả request + response
            User u = rememberMeService.autoLogin(req, res);

            if (u != null) {
                req.getSession(true).setAttribute("user", u);
                System.out.println("[RememberMeFilter] Auto login success: " + u.getEmail());
            } else {
                System.out.println("[RememberMeFilter] Auto login failed or expired cookie.");
            }
        }

        chain.doFilter(request, response);
    }
}
