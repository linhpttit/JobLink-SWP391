package com.joblink.joblink.service;

import com.joblink.joblink.auth.model.BasicInfoUpdate;
import com.joblink.joblink.auth.model.JobSeekerProfile;
import com.joblink.joblink.auth.model.ProfileCompletion;
import com.joblink.joblink.dao.UserDao;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final UserDao userDao;
    private final HttpSession session;

    public ProfileServiceImpl(UserDao userDao, HttpSession session) {
        this.userDao = userDao;
        this.session = session;
    }

    @Override
    public JobSeekerProfile getOrInitProfile(int userId) {
        return userDao.findProfileMinimalById(userId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng #" + userId));
    }

    @Override
    public Optional<JobSeekerProfile> findProfile(int userId) {
        return userDao.findProfileMinimalById(userId);
    }

    @Override
    public void updateBasicInfo(int userId, BasicInfoUpdate req) {
        // BẢNG users: full_name / username / email đều NOT NULL
        // => không hỗ trợ xoá/clear, chỉ update giá trị hợp lệ
        String fullName = trimOrNull(req.getFullName());
        String username = trimOrNull(req.getUsername());
        String email    = trimOrNull(req.getEmail());
        userDao.updateBasicInfo(userId, fullName, username, email);
    }

    @Override
    public String updateAvatarInSession(int userId, String avatarUrl) {
        @SuppressWarnings("unchecked")
        Map<Integer, String> avatars = (Map<Integer, String>) session.getAttribute("__avatar_map__");
        if (avatars == null) {
            avatars = new HashMap<>();
            session.setAttribute("__avatar_map__", avatars);
        }
        avatars.put(userId, avatarUrl);
        return avatarUrl;
    }

    @Override
    public ProfileCompletion computeCompletion(int userId) {
        JobSeekerProfile p = getOrInitProfile(userId);

        int score = 0;
        int max = 100;
        List<String> missing = new ArrayList<>();

        // Tạm chấm 40 điểm cho 3 trường cơ bản có thật trong DB
        int nameW = 15, usernameW = 15, emailW = 10;
        if (nz(p.getFullName())) score += nameW; else missing.add("Full name");
        if (nz(p.getUsername())) score += usernameW; else missing.add("Username");
        if (nz(p.getEmail()))    score += emailW; else missing.add("Email");

        // Các phần khác chưa có cột để lưu -> hiển thị là thiếu để gợi ý thêm
        Collections.addAll(missing,
                "Avatar", "Headline", "About", "Phone", "Location",
                "Skills", "Experiences", "Educations", "Languages", "Projects");

        ProfileCompletion out = new ProfileCompletion();
        out.setPercent(Math.min(max, Math.max(0, score)));
        out.setMissingFields(missing);
        return out;
    }

    @Override
    public void assertOwnership(int userIdFromPath, int currentUserId) {
        if (userIdFromPath != currentUserId) {
            throw new IllegalStateException("Bạn không có quyền truy cập hồ sơ này.");
        }
    }

    /* helpers */
    private static String trimOrNull(String s) { return (s == null ? null : s.trim()); }
    private static boolean nz(String s) { return s != null && !s.isBlank(); }
}
