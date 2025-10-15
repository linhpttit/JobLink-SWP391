package com.joblink.joblink.controller;

import com.joblink.joblink.dto.EmployerProfileDto;
import com.joblink.joblink.dto.JobPostingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import com.joblink.joblink.service.IEmployerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String viewPasswordPage(){ return "employer/password";  }
    @GetMapping("/job-posting")
    public String viewJobPostingPage() { return "employer/job-post"; }

    @PostMapping("/password")
    public String changePassword(
        @RequestParam("currentPassword") String currentPass,
        @RequestParam("newPassword") String newPass,
        @RequestParam("confirmPassword") String confirmPass,
        Model model){
        boolean result = employerService.changePassword(currentPass,newPass,confirmPass);
        if (result) {
            model.addAttribute("message", "Đổi mật khẩu thành công!");
            model.addAttribute("pageCss", "/password.css"); // CSS riêng
            return "redirect:/employer/password"; // về trang
        } else {
            model.addAttribute("error", "Mật khẩu không hợp lệ hoặc xác nhận sai!");
            model.addAttribute("pageCss", "/password.css"); // CSS riêng
            return "password"; // hiển thị lại form
        }
    }
    @GetMapping("/profile")
    public String getProfile(Model model){
        EmployerProfileDto employerProfileDto = employerService.getActiveEmployerProfile();
                model.addAttribute("profile", employerProfileDto);
                return "employer/profile";
    }

    @PostMapping("/profile")
    public String editProfile(
            @ModelAttribute EmployerProfileDto profileDto,
            RedirectAttributes redirectAtt)
    {
                try{
                    employerService.editProfile(profileDto);
                    redirectAtt.addFlashAttribute("message", "Cập nhật thành công!");
                }catch (IllegalArgumentException e){
                    redirectAtt.addFlashAttribute("error", e.getMessage());
                }
                return "redirect:/employer/profile";
    }

}
