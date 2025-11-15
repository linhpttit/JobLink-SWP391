package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.model.JobSeekerProfile2;
<<<<<<< HEAD
import com.joblink.joblink.service.ApplicationService2;
=======
import com.joblink.joblink.service.ApplicationService;
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141
import com.joblink.joblink.service.PremiumService;
import com.joblink.joblink.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobseeker/applied-companies")
public class    AppliedCompaniesController {
<<<<<<< HEAD
    private final ApplicationService2 applicationService2;
    private final PremiumService premiumService;
    private final ProfileService profileService;

    public AppliedCompaniesController(ApplicationService2 applicationService2,
                                      PremiumService premiumService,
                                      ProfileService profileService) {
        this.applicationService2 = applicationService2;
=======
    private final ApplicationService applicationService;
    private final PremiumService premiumService;
    private final ProfileService profileService;

    public AppliedCompaniesController(ApplicationService applicationService,
                                      PremiumService premiumService,
                                      ProfileService profileService) {
        this.applicationService = applicationService;
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141
        this.premiumService = premiumService;
        this.profileService = profileService;
    }

    @GetMapping
    public String showAppliedCompanies(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";

        // (Tuỳ chọn) nếu bạn muốn trang này chỉ cho premium thì bật lại check sau:
        // boolean hasMessagingAccess = premiumService.hasFeature(user.getUserId(), "messaging");
        // if (!hasMessagingAccess) return "redirect:/jobseeker/premium";

        JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId()); // ✅ đúng khoá
<<<<<<< HEAD
        List<Map<String, Object>> appliedCompanies = applicationService2.getAppliedCompaniesForSeeker(profile.getSeekerId());
=======
        List<Map<String, Object>> appliedCompanies = applicationService.getAppliedCompaniesForSeeker(profile.getSeekerId());
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141

        model.addAttribute("appliedCompanies", appliedCompanies);
        model.addAttribute("user", user);
        return "applied-companies";
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAppliedCompaniesAPI(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();

        JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId()); // ✅ đúng khoá
<<<<<<< HEAD
        List<Map<String, Object>> appliedCompanies = applicationService2.getAppliedCompaniesForSeeker(profile.getSeekerId());
=======
        List<Map<String, Object>> appliedCompanies = applicationService.getAppliedCompaniesForSeeker(profile.getSeekerId());
>>>>>>> 5b84532ce7c137b8c9bb0033ca31dc467a3e2141
        return ResponseEntity.ok(appliedCompanies);
    }
}
