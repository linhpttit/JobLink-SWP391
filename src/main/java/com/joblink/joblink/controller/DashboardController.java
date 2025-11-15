package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.model.CVUpload;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobseeker/dashboardjobseeker")
public class DashboardController {
    private final ProfileService profileService;
    private final CVUploadService cvUploadService;
    private final DashBoardJobSeeker dashBoardJobSeeker;

    public DashboardController(ProfileService profileService,
                               CVUploadService cvUploadService,
                               DashBoardJobSeeker dashBoardJobSeeker) {
        this.profileService = profileService;
        this.cvUploadService = cvUploadService;
        this.dashBoardJobSeeker = dashBoardJobSeeker;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) return "redirect:/signin";
        if (!"seeker".equalsIgnoreCase(user.getRole())) return "redirect:/signin";

        JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());

        Map<String, Object> dashboardData = dashBoardJobSeeker.getCompleteDashboardData(profile.getSeekerId());

        model.addAttribute("user", user);
        model.addAttribute("profile", dashboardData.get("profile"));
        model.addAttribute("mostRecentCV", dashboardData.get("mostRecentCV"));
        model.addAttribute("stats", dashboardData.get("statistics"));

        return "dashboardjobseeker";
    }
}
