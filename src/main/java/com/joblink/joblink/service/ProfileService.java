package com.joblink.joblink.service;

import com.joblink.joblink.auth.model.JobSeekerProfile;
import com.joblink.joblink.auth.model.BasicInfoUpdate;
import com.joblink.joblink.auth.model.ProfileCompletion;

import java.util.Optional;

public interface ProfileService {

    // Lấy hoặc khởi tạo hồ sơ tối thiểu (dựa vào joblink.users)
    JobSeekerProfile getOrInitProfile(int userId);
    Optional<JobSeekerProfile> findProfile(int userId);

    // Cập nhật thông tin cơ bản (lưu vào joblink.users)
    void updateBasicInfo(int userId, BasicInfoUpdate req);

    // Avatar: tạm thời giữ trong session (không có cột để lưu bền)
    String updateAvatarInSession(int userId, String avatarUrl);

    // Tính % hoàn thiện & phần còn thiếu (dựa vào dữ liệu thực có trong joblink.users)
    ProfileCompletion computeCompletion(int userId);

    // Bảo vệ quyền sở hữu hồ sơ
    void assertOwnership(int userIdFromPath, int currentUserId);
}
