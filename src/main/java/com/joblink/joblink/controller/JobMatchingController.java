
package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.model.JobSeekerProfile;
import com.joblink.joblink.model.PremiumPackage;
import com.joblink.joblink.model.PremiumSubscription;
import com.joblink.joblink.service.JobMatchingService;
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
@RequestMapping("/jobseeker/job-matching")
public class JobMatchingController {
    private final JobMatchingService jobMatchingService;
    private final PremiumService premiumService;
    private final ProfileService profileService;

    public JobMatchingController(JobMatchingService jobMatchingService,
                                 PremiumService premiumService,
                                 ProfileService profileService) {
        this.jobMatchingService = jobMatchingService;
        this.premiumService = premiumService;
        this.profileService = profileService;
    }

    @GetMapping("/top-matches")
    public String showTopMatches(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Check if user has SuperStar or LegendStar package
        PremiumSubscription subscription = premiumService.getActiveSubscription(user.getUserId());
        if (subscription == null) {
            model.addAttribute("error", "Bạn cần nâng cấp lên gói SuperStar-Premium hoặc LegendStar-Premium");
            return "redirect:/jobseeker/premium";
        }

        PremiumPackage pkg = premiumService.getPackageById(subscription.getPackageId());
        if (pkg == null || (!pkg.getCode().equals("SUPERSTAR_PREMIUM") && !pkg.getCode().equals("LEGENDSTAR_PREMIUM"))) {
            model.addAttribute("error", "Tính năng này chỉ dành cho gói SuperStar-Premium và LegendStar-Premium");
            return "redirect:/jobseeker/premium";
        }

        JobSeekerProfile profile = profileService.getProfileBySeekerId(user.getUserId());
        List<Map<String, Object>> topMatches = jobMatchingService.getTopMatchingJobs(profile.getSeekerId(), 5);

        model.addAttribute("topMatches", topMatches);
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);

        return "job-matching-results";
    }

    @GetMapping("/api/top-matches")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTopMatchesAPI(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        PremiumSubscription subscription = premiumService.getActiveSubscription(user.getUserId());
        if (subscription == null) {
            return ResponseEntity.status(403).build();
        }

        JobSeekerProfile profile = profileService.getProfileBySeekerId(user.getUserId());
        List<Map<String, Object>> topMatches = jobMatchingService.getTopMatchingJobs(profile.getSeekerId(), 5);

        return ResponseEntity.ok(topMatches);
    }
}
