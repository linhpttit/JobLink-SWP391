package com.joblink.joblink.config;

import com.joblink.joblink.auth.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/signin", "/signup", "/verify-otp",
                                "/forgot/**", "/css/**", "/js/**", "/images/**", "/jobs/**",
                                "/search", "/employers", "/job-detail/**").permitAll()
                        .requestMatchers("/auth/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/signin")
                        .loginProcessingUrl("/process-login")
                        .defaultSuccessUrl("/", true) // Form login về trang chủ
                        .failureUrl("/signin?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/signin")
                        //.successHandler(oauth2SuccessHandler()) // ✅ custom handler
                        .redirectionEndpoint(redir -> redir.baseUri("/auth/login/oauth2/code/*")) // ép callback path vào vùng /auth
                        .defaultSuccessUrl("/", true) // ✅ THÊM DÒNG NÀY
                        .successHandler(oauth2SuccessHandler()) // ✅ custom handler
                        .failureUrl("/signin?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            request.getSession().invalidate();
                            response.sendRedirect("/");
                        })
                );

        return http.build();
    }

    // ✅ Custom success handler cho Google login
    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            HttpSession session = request.getSession();

            // Lấy thông tin user từ OAuth2
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

            // Tạo user object từ OAuth2 attributes
            User oauthUser = createUserFromOAuthAttributes(attributes);
            session.setAttribute("user", oauthUser);

            // ✅ LUÔN redirect về trang chủ
            response.sendRedirect("/");
        };
    }

    private User createUserFromOAuthAttributes(Map<String, Object> attributes) {
        User user = new User();
        user.setEmail((String) attributes.get("email"));
        user.setFullName((String) attributes.get("name"));
        //suser.setRole("seeker"); // Mặc định là seeker
        return user;
    }
}