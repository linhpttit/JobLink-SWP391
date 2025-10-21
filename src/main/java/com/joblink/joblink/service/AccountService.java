package com.joblink.joblink.service;

import com.joblink.joblink.dao.BlockedEmployerDao;
import com.joblink.joblink.dao.JobSeekerProfileDao;
import com.joblink.joblink.dao.UserDao;
import com.joblink.joblink.auth.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final UserDao userDao;
    private final AuthService authService;
    private final JobSeekerProfileDao profileDao;
    private final BlockedEmployerDao blockedEmployerDao;

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
     */
    @Transactional
    public boolean deactivateAccount(int userId) {
        // Tên hàm đã đổi cho rõ nghĩa hơn
        return userDao.setHidden(userId, false) > 0;
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