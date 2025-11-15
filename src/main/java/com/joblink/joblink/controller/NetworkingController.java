package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.model.PremiumSubscription;
<<<<<<< HEAD
import com.joblink.joblink.service.ApplicationService2;
=======
import com.joblink.joblink.service.ApplicationService;
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141
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
<<<<<<< HEAD
    private final ApplicationService2 applicationService2;
=======
    private final ApplicationService applicationService;
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141

    public NetworkingController(JobSeekerService jobSeekerService,
                                ProfileService profileService,
                                PremiumService premiumService,
<<<<<<< HEAD
                                ApplicationService2 applicationService2) {
        this.jobSeekerService = jobSeekerService;
        this.profileService = profileService;
        this.premiumService = premiumService;
        this.applicationService2 = applicationService2;
=======
                                ApplicationService applicationService) {
        this.jobSeekerService = jobSeekerService;
        this.profileService = profileService;
        this.premiumService = premiumService;
        this.applicationService = applicationService;
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141
    }

    @GetMapping
    public String networkingPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"seeker".equalsIgnoreCase(user.getRole())) {
            return "redirect:/auth/login";
        }

        // Lấy profile theo userId (chuẩn JDBC)
        JobSeekerProfile2 me = profileService.getOrCreateProfile(user.getUserId());
        if (me == null) {
            return "redirect:/jobseeker/profile";
        }

        // Luôn tính danh sách seeker có skill trùng
        List<JobSeekerProfile2> similarSeekers = Collections.emptyList();
        try {
            similarSeekers = jobSeekerService.findSeekersWithOverlappingSkills(me.getSeekerId());
        } catch (Exception ignored) { }

        // Nếu premium: bổ sung danh sách employer đã apply
        PremiumSubscription sub = premiumService.getActiveSubscription(user.getUserId());
        boolean hasPremium = (sub != null);

        List<Map<String, Object>> appliedCompanies = Collections.emptyList();
        if (hasPremium) {
            try {
<<<<<<< HEAD
                appliedCompanies = applicationService2.getAppliedCompaniesForSeeker(me.getSeekerId());
=======
                appliedCompanies = applicationService.getAppliedCompaniesForSeeker(me.getSeekerId());
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141
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
