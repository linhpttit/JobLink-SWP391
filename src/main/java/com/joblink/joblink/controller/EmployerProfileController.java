package com.joblink.joblink.controller;

import com.joblink.joblink.dto.EmployerProfileDto;
import com.joblink.joblink.dto.UserSessionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import com.joblink.joblink.service.IEmployerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/employer")
@RequiredArgsConstructor
public class EmployerProfileController {
    private final IEmployerService employerService;

    @GetMapping
    public String getProfile(){
        return "employer/employer-layout";
    }

    @GetMapping("/password")
    public String viewPasswordPage(){
        return "employer/password";
    }

    @GetMapping("/job-posting")
    public String viewJobPostingPage() {
        return "employer/job-post";
    }

    @PostMapping("/password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPass,
            @RequestParam("newPassword") String newPass,
            @RequestParam("confirmPassword") String confirmPass,
            HttpSession session,
            Model model,
            RedirectAttributes ra){

        // Lấy user từ session
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        boolean result = employerService.changePassword(user.getUserId(), currentPass, newPass, confirmPass);
        if (result) {
            ra.addFlashAttribute("message", "Đổi mật khẩu thành công!");
            return "redirect:/employer/password";
        } else {
            model.addAttribute("error", "Mật khẩu không hợp lệ hoặc xác nhận sai!");
            model.addAttribute("pageCss", "/password.css");
            return "employer/password";
        }
    }

    @GetMapping("/profile")
    public String getProfile(HttpSession session, Model model, RedirectAttributes ra){
        // Lấy user từ session
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        EmployerProfileDto employerProfileDto = employerService.getActiveEmployerProfile(user.getUserId());
        model.addAttribute("profile", employerProfileDto);
        return "employer/profile";
    }

    @PostMapping("/profile")
    public String editProfile(
            @ModelAttribute EmployerProfileDto profileDto,
            HttpSession session,
            RedirectAttributes redirectAtt)
    {
        // Lấy user từ session
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            redirectAtt.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        try{
            employerService.editProfile(user.getUserId(), profileDto);
            redirectAtt.addFlashAttribute("message", "Cập nhật thành công!");
        }catch (IllegalArgumentException e){
            redirectAtt.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employer/profile";
    }
}