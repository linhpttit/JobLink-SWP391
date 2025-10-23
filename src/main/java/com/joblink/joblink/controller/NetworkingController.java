package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.model.JobSeekerProfile;
import com.joblink.joblink.model.PremiumSubscription;
import com.joblink.joblink.service.ApplicationService;
import com.joblink.joblink.service.JobSeekerService;
import com.joblink.joblink.service.PremiumService;
import com.joblink.joblink.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobseeker/networking")
public class NetworkingController {

    private final JobSeekerService jobSeekerService;
    private final ProfileService profileService;
    private final PremiumService premiumService;
    private final ApplicationService applicationService;

    public NetworkingController(JobSeekerService jobSeekerService,
                                ProfileService profileService,
                                PremiumService premiumService,
                                ApplicationService applicationService) {
        this.jobSeekerService = jobSeekerService;
        this.profileService = profileService;
        this.premiumService = premiumService;
        this.applicationService = applicationService;
    }

    @GetMapping
    public String networkingPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"seeker".equalsIgnoreCase(user.getRole())) {
            return "redirect:/auth/login";
        }

        // Lấy profile theo userId (chuẩn JDBC)
        JobSeekerProfile me = profileService.getOrCreateProfile(user.getUserId());
        if (me == null) {
            return "redirect:/jobseeker/profile";
        }

        // Luôn tính danh sách seeker có skill trùng
        List<JobSeekerProfile> similarSeekers = Collections.emptyList();
        try {
            similarSeekers = jobSeekerService.findSeekersWithOverlappingSkills(me.getSeekerId());
        } catch (Exception ignored) { }

        // Nếu premium: bổ sung danh sách employer đã apply
        PremiumSubscription sub = premiumService.getActiveSubscription(user.getUserId());
        boolean hasPremium = (sub != null);

        List<Map<String, Object>> appliedCompanies = Collections.emptyList();
        if (hasPremium) {
            try {
                appliedCompanies = applicationService.getAppliedCompaniesForSeeker(me.getSeekerId());
            } catch (Exception ignored) { }
        }

        model.addAttribute("user", user);
        model.addAttribute("currentProfile", me);
        model.addAttribute("similarSeekers", similarSeekers);
        model.addAttribute("hasPremium", hasPremium);
        model.addAttribute("appliedCompanies", appliedCompanies);

        return "networking";
    }
}
