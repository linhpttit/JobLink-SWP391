package com.joblink.joblink.config;



import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.security.RememberMeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RememberMeService rememberMeService;

    public WebConfig(RememberMeService rememberMeService) {
        this.rememberMeService = rememberMeService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
                HttpSession session = req.getSession(false);
                if (session != null && session.getAttribute("user") != null) return true;

                User u = rememberMeService.autoLogin(req);
                if (u != null) {
                    req.getSession(true).setAttribute("user", u);
                }
                return true;
            }
        }).addPathPatterns("/**"); // check cho mọi request
    }
}
