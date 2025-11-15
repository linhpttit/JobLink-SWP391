package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.service.JobPostingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employer-current-job-list")
public class EmployerCurrentJobController {
    private final JobPostingService jobPostingService;

    @GetMapping
    public String viewCurrentJobList(Model model,
                                     HttpSession session,
                                     RedirectAttributes ra) {

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        Long currentEmployerId = Long.valueOf(user.getUserId());

        // Lấy toàn bộ bài đăng của employer
        List<JobPosting> jobs = jobPostingService.findJobPostingsByEmployer(currentEmployerId);

        model.addAttribute("jobs", jobs);
        model.addAttribute("totalPosts", jobs.size());

        return "employer/current-job-list";
    }
}