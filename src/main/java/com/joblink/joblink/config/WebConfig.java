package com.joblink.joblink.config;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.security.RememberMeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RememberMeService rememberMeService;

    public WebConfig(RememberMeService rememberMeService) {
        this.rememberMeService = rememberMeService;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/login", "/signin");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:src/main/resources/static/uploads/avatars/");
        registry.addResourceHandler("/certificates/**")
                .addResourceLocations("file:src/main/resources/static/uploads/certificates/");
        registry.addResourceHandler("/cvs/**")
                .addResourceLocations("file:src/main/resources/static/uploads/cvs/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
                        HttpSession session = req.getSession(false);

                        // Nếu đã có session user → bỏ qua
                        if (session != null && session.getAttribute("user") != null) {
                            return true;
                        }

                        // Thử auto-login bằng cookie REMEMBER
                        User u = rememberMeService.autoLogin(req, res);
                        if (u != null) {
                            req.getSession(true).setAttribute("user", u);
                            System.out.println("[WebConfig] ✅ Auto-login from cookie: " + u.getEmail());
                        } else {
                            System.out.println("[WebConfig] ⚠️ No valid REMEMBER cookie or expired session.");
                        }

                        return true; // Cho phép request tiếp tục
                    }
                })
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // Auth routes
                        "/signin", "/signup", "/login", "/logout",
                        "/verify-otp", "/resend-otp",
                        "/forgot", "/forgot/**",

                        // API public
                        "/api/session-check",

                        // Static resources
                        "/css/**", "/js/**", "/images/**", "/static/**", "/webjars/**",
                        "/avatars/**", "/certificates/**", "/cvs/**", "/uploads/**",

                        // Error
                        "/error", "/404"
                );
    }
}
