package com.joblink.joblink.service;

import com.joblink.joblink.dao.BlockedEmployerDao;
import com.joblink.joblink.dao.JobSeekerProfileDao;
import com.joblink.joblink.dao.UserDao;
import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.Repository.JobSeekerProfileRepository;
import com.joblink.joblink.entity.JobSeekerProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountService {
    private final UserDao userDao;
    private final AuthService authService;
    private final JobSeekerProfileDao profileDao;
    private final BlockedEmployerDao blockedEmployerDao;
    @Autowired
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    public AccountService(UserDao userDao,
                          AuthService authService,
                          JobSeekerProfileDao profileDao,
                          BlockedEmployerDao blockedEmployerDao) {
        this.userDao = userDao;
        this.authService = authService;
        this.profileDao = profileDao;
        this.blockedEmployerDao = blockedEmployerDao;
    }

    /**
     * Thay đổi mật khẩu, yêu cầu xác thực mật khẩu cũ.
     */
    @Transactional
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        if (email == null || currentPassword == null || newPassword == null) return false;
        User u = authService.authenticate(email, currentPassword);
        if (u == null) return false; // Mật khẩu cũ không đúng

        int rows = userDao.resetPassword(email, newPassword);
        return rows > 0;
    }

    /**
     * Vô hiệu hóa tài khoản (xóa mềm).
     * Cập nhật cả User.enabled = false và JobSeekerProfile (nếu có).
     */
    @Transactional
    public boolean deactivateAccount(int userId) {
        try {
            // 1. Vô hiệu hóa tài khoản User để không thể đăng nhập
            int rowsAffected = userDao.setEnabled(userId, false);
            
            if (rowsAffected <= 0) {
                System.err.println("❌ Không tìm thấy User để vô hiệu hóa, ID: " + userId);
                return false;
            }
            
            System.out.println("✅ Đã vô hiệu hóa User ID: " + userId);
            
            // 2. Nếu là jobseeker, cập nhật JobSeekerProfile để khóa
            try {
                JobSeekerProfile profile = jobSeekerProfileRepository.findByUserId(userId).orElse(null);
                if (profile != null) {
                    profile.setIsLocked(true);
                    profile.setReceiveInvitations(false);
                    profile.setUpdatedAt(LocalDateTime.now());
                    jobSeekerProfileRepository.save(profile);
                    System.out.println("✅ Đã cập nhật JobSeekerProfile ID: " + profile.getSeekerId() + " - đánh dấu là đã khóa");
                }
            } catch (Exception e) {
                // Nếu không phải jobseeker hoặc không tìm thấy profile, không sao
                System.out.println("ℹ️ Không tìm thấy JobSeekerProfile cho User ID: " + userId + " (có thể là employer hoặc chưa tạo profile)");
            }
            
            return true;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi vô hiệu hóa tài khoản: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật cài đặt nhận lời mời việc làm.
     */
    @Transactional
    public boolean updateInvitationSetting(int seekerId, boolean enabled) {
        return profileDao.updateReceiveInvitations(seekerId, enabled) > 0;
    }

    /**
     * Chặn một nhà tuyển dụng.
     */
    @Transactional
    public void blockEmployer(int seekerId, int employerId) {
        // TODO: Có thể thêm logic kiểm tra giới hạn 5 công ty ở đây
        blockedEmployerDao.block(seekerId, employerId);
    }

    /**
     * Bỏ chặn một nhà tuyển dụng.
     */
    @Transactional
    public void unblockEmployer(int seekerId, int employerId) {
        // Tên hàm đã đổi cho nhất quán
        blockedEmployerDao.unblock(seekerId, employerId);
    }
}