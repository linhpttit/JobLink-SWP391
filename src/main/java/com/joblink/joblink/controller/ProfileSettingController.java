// File: ProfileSettingController.java (PHIÊN BẢN HOÀN CHỈNH ĐÃ HỢP NHẤT)
package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.service.AccountService;
import com.joblink.joblink.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProfileSettingController {

    // Inject tất cả các service cần thiết
    private final AccountService accountService;
    private final ProfileService profileService;

    /**
     * Phương thức GET: Hiển thị trang cài đặt hồ sơ.
     */
    @GetMapping("/jobseeker/profile-setting")
    public String profileSettingPage(HttpSession session, Model model, RedirectAttributes ra) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Please sign in to access settings");
            return "redirect:/signin";
        }

        JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        // TODO: Lấy thêm dữ liệu như invitationCount, danh sách công ty đã chặn... và đưa vào model

        return "profile-setting";
    }

    /**
     * Phương thức POST: Xử lý yêu cầu thay đổi mật khẩu.
     */
    @PostMapping("/settings/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Please sign in");
            return "redirect:/signin";
        }

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/jobseeker/profile-setting";
        }

        boolean ok = accountService.changePassword(user.getEmail(), currentPassword, newPassword);
        if (ok) {
            ra.addFlashAttribute("success", "Password changed successfully.");
        } else {
            ra.addFlashAttribute("error", "Failed to change password. Please check your current password.");
        }
        return "redirect:/jobseeker/profile-setting";
    }

    /**
     * Phương thức POST: Xử lý bật/tắt nhận lời mời việc làm.
     */
    @PostMapping("/settings/toggle-invitations")
    public ResponseEntity<Void> toggleInvitations(@RequestBody Map<String, Boolean> payload, HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        int seekerId = profileService.getOrCreateProfile(user.getUserId()).getSeekerId();
        boolean enabled = payload.getOrDefault("enabled", false);
        accountService.updateInvitationSetting(seekerId, enabled);
        return ResponseEntity.ok().build();
    }

    /**
     * Phương thức POST: Xử lý yêu cầu xóa (vô hiệu hóa) tài khoản.
     */
    @PostMapping("/settings/delete-account")
    public ResponseEntity<Map<String, Object>> deleteAccount(HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not authenticated."));
        }

        boolean ok = accountService.deactivateAccount(user.getUserId());
        if (ok) {
            session.invalidate(); // Xóa session sau khi vô hiệu hóa tài khoản
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to deactivate account."));
        }
    }
}