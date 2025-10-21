package com.joblink.joblink.service;

import com.joblink.joblink.dto.EmployerProfileDto;

public interface IEmployerService {

    /**
     * Đổi mật khẩu cho employer
     * @param userId ID của user từ session (kiểu Integer)
     * @param curPass Mật khẩu hiện tại
     * @param newPass Mật khẩu mới
     * @param confirmPass Xác nhận mật khẩu mới
     * @return true nếu đổi thành công, false nếu thất bại
     */
    boolean changePassword(Integer userId, String curPass, String newPass, String confirmPass);

    /**
     * Chỉnh sửa profile của employer
     * @param userId ID của user từ session (kiểu Integer)
     * @param employerProfileDto DTO chứa thông tin cập nhật
     * @throws IllegalArgumentException nếu email hoặc phone đã tồn tại
     */
    void editProfile(Integer userId, EmployerProfileDto employerProfileDto);

    /**
     * Lấy profile của employer hiện tại
     * @param userId ID của user từ session (kiểu Integer)
     * @return EmployerProfileDto chứa thông tin profile
     */
    EmployerProfileDto getActiveEmployerProfile(Integer userId);
}