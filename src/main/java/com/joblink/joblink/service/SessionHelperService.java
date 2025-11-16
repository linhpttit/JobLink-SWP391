package com.joblink.joblink.service;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.Repository.EmployerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionHelperService {

    private final EmployerRepository employerRepository;

    /**
     * Lấy employerId từ session user hiện tại
     */
    public Long getCurrentEmployerId() {
        UserSessionDTO userSession = getCurrentUserSession();

        if (userSession == null) {
            throw new SecurityException("User not logged in");
        }

        if (!"employer".equalsIgnoreCase(userSession.getRole())) {
            throw new SecurityException("Access denied: Employer role required. Current role: " + userSession.getRole());
        }

        // Tìm employer profile từ user ID - SỬ DỤNG METHOD CÓ SẴN TRONG REPOSITORY
        Optional<Employer> employer = employerRepository.findByUserId(userSession.getUserId());

        if (employer.isEmpty()) {
            throw new RuntimeException("Employer profile not found for user ID: " + userSession.getUserId());
        }

        return employer.get().getId(); // employer_id
    }

    /**
     * Lấy thông tin user từ session
     */
    public UserSessionDTO getCurrentUserSession() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes())
                    .getRequest();
            HttpSession session = request.getSession(false);

            if (session != null) {
                return (UserSessionDTO) session.getAttribute("user");
            }
            return null;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Kiểm tra user có phải employer không
     */
    public boolean isEmployer() {
        try {
            UserSessionDTO userSession = getCurrentUserSession();
            return userSession != null && "employer".equalsIgnoreCase(userSession.getRole());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lấy userId từ session
     */
    public Integer getCurrentUserId() {
        UserSessionDTO userSession = getCurrentUserSession();
        if (userSession == null) {
            throw new SecurityException("User not logged in");
        }
        return userSession.getUserId();
    }
}