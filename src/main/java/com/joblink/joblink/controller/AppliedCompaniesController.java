package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.service.ApplicationService;
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
    private final ApplicationService applicationService;
    private final PremiumService premiumService;
    private final ProfileService profileService;

    public AppliedCompaniesController(ApplicationService applicationService,
                                      PremiumService premiumService,
                                      ProfileService profileService) {
        this.applicationService = applicationService;
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
        List<Map<String, Object>> appliedCompanies = applicationService.getAppliedCompaniesForSeeker(profile.getSeekerId());

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
        List<Map<String, Object>> appliedCompanies = applicationService.getAppliedCompaniesForSeeker(profile.getSeekerId());
        return ResponseEntity.ok(appliedCompanies);
    }
}
