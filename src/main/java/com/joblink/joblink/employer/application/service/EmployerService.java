package com.joblink.joblink.employer.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.repository.EmployerRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("NewEmployerService")
@RequiredArgsConstructor
@Slf4j
public class EmployerService implements IEmployerService {

    private final EmployerRepository repo;
    private final HttpSession session;

    @Override
    public Employer getById(Long id){
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employer không tồn tại"));
    }

    @Override
    public Long getCurrentEmployerId() {
        Object obj = session.getAttribute("user");
        if (!(obj instanceof UserSessionDTO user)) {
            log.warn("getCurrentEmployerId: no session user");
            throw new IllegalStateException("Không xác định được người dùng hiện tại (chưa đăng nhập).");
        }
        if (user.getRole() == null || !"employer".equalsIgnoreCase(user.getRole())) {
            log.warn("getCurrentEmployerId: wrong role userId={} role={}", user.getUserId(), user.getRole());
            throw new AccessDeniedException("Bạn không có quyền nhà tuyển dụng.");
        }

        // 1) Try by userId (preferred if Employer links to User)
        if (user.getUserId() != null) {
            return repo.findByUserId(user.getUserId())
                    // or .findByUser_UserId(user.getUserId()) if your mapping is nested
                    .map(Employer::getId)
                    .orElseGet(() -> fallbackByEmail(user));
        }

        // 2) Fallback by email
        return fallbackByEmail(user);
    }

    private Long fallbackByEmail(UserSessionDTO user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalStateException("Không tìm thấy Employer cho tài khoản hiện tại (thiếu email).");
        }
        return repo.findFirstByUserEmailIgnoreCase(user.getEmail())
                // or findFirstByContactEmailIgnoreCase(...) if that’s your field
                .map(Employer::getId)
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy hồ sơ nhà tuyển dụng cho email " + user.getEmail()));
    }

    @Override
    public Employer getCurrentEmployer() {
        return getById(getCurrentEmployerId());
    }
}
