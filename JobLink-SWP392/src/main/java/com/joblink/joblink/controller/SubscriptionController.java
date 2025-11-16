package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.service.EmailSuggestionService;
import com.joblink.joblink.service.ProfileService;
import com.joblink.joblink.service.SubscriptionSeekerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@RequestMapping("/jobseeker")
public class SubscriptionController {

    // ✅ Constante pour éviter les erreurs de clé de session
    private static final String SESSION_USER_KEY = "user";

    private final SubscriptionSeekerService subscriptionService;
    private final EmailSuggestionService emailSuggestionService;
    private final ProfileService profileService;

    public SubscriptionController(SubscriptionSeekerService subscriptionService,
                                  EmailSuggestionService emailSuggestionService,
                                  ProfileService profileService) {
        this.subscriptionService = subscriptionService;
        this.emailSuggestionService = emailSuggestionService;
        this.profileService = profileService;
    }

    /**
     * Display email subscriptions management page
     */
    @GetMapping("/email-subscriptions")
    public String emailSubscriptionsPage(HttpSession session, Model model) {
        // ✅ Utilise la bonne clé "user" au lieu de "UserSessionDTO"
        UserSessionDTO userSessionDTO = (UserSessionDTO) session.getAttribute(SESSION_USER_KEY);

        if (userSessionDTO == null) {
            return "redirect:/signin";
        }

        // ✅ Récupérer le seeker_id depuis JobSeekerProfile
        JobSeekerProfile2 profile = profileService.getOrCreateProfile(userSessionDTO.getUserId());
        int seekerId = profile.getSeekerId();

        // Get all available skills and provinces for selection
        List<Map<String, Object>> availableSkills = subscriptionService.getAllAvailableSkills();
        List<Map<String, Object>> availableProvinces = subscriptionService.getAllProvinces();

        // Get current subscriptions
        List<Map<String, Object>> userSubscriptions = subscriptionService.getSubscriptionsBySeeker(seekerId);

        // Get email statistics
        Map<String, Object> stats = subscriptionService.getSubscriptionStats(seekerId);

        model.addAttribute("user", userSessionDTO);
        model.addAttribute("seekerId", seekerId); // ✅ Ajouter seekerId au model
        model.addAttribute("availableSkills", availableSkills);
        model.addAttribute("availableProvinces", availableProvinces);
        model.addAttribute("userSubscriptions", userSubscriptions);
        model.addAttribute("stats", stats);

        return "email-subscriptions";
    }

    /**
     * API: Get all available skills
     */
    @GetMapping("/api/subscription-skills")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAvailableSkills() {
        List<Map<String, Object>> skills = subscriptionService.getAllAvailableSkills();
        return ResponseEntity.ok(skills);
    }

    /**
     * API: Get all available provinces
     */
    @GetMapping("/api/subscription-provinces")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAvailableProvinces() {
        List<Map<String, Object>> provinces = subscriptionService.getAllProvinces();
        return ResponseEntity.ok(provinces);
    }

    /**
     * API: Get subscriptions for logged-in user
     */
    @GetMapping("/api/my-subscriptions")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getMySubscriptions(HttpSession session) {
        // ✅ Corrigé: utilise SESSION_USER_KEY
        UserSessionDTO userSessionDTO = (UserSessionDTO) session.getAttribute(SESSION_USER_KEY);

        if (userSessionDTO == null) {
            return ResponseEntity.status(401).build();
        }

        // ✅ Récupérer le seeker_id depuis JobSeekerProfile
        JobSeekerProfile2 profile = profileService.getOrCreateProfile(userSessionDTO.getUserId());
        int seekerId = profile.getSeekerId();

        List<Map<String, Object>> subscriptions = subscriptionService.getSubscriptionsBySeeker(seekerId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * API: Create new subscription
     */
    @PostMapping("/api/create-subscription")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createSubscription(
            @RequestParam int skillId,
            @RequestParam int provinceId,
            HttpSession session) {

        // ✅ Corrigé: utilise SESSION_USER_KEY
        UserSessionDTO userSessionDTO = (UserSessionDTO) session.getAttribute(SESSION_USER_KEY);

        if (userSessionDTO == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // ✅ Récupérer le seeker_id depuis JobSeekerProfile
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(userSessionDTO.getUserId());
            int seekerId = profile.getSeekerId();

            int subscriptionId = subscriptionService.createSubscription(seekerId, skillId, provinceId);

            // Get skill and province names for email
            List<Map<String, Object>> skills = subscriptionService.getAllAvailableSkills();
            List<Map<String, Object>> provinces = subscriptionService.getAllProvinces();

            String skillName = skills.stream()
                    .filter(s -> s.get("skill_id").equals(skillId))
                    .map(s -> (String) s.get("skill_name"))
                    .findFirst()
                    .orElse("Skill");

            String provinceName = provinces.stream()
                    .filter(p -> p.get("province_id").equals(provinceId))
                    .map(p -> (String) p.get("province_name"))
                    .findFirst()
                    .orElse("Province");

            // Send confirmation email
            emailSuggestionService.sendSubscriptionConfirmationEmail(
                    userSessionDTO.getEmail(),
                    userSessionDTO.getFullName(),
                    skillName,
                    provinceName
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptionId", subscriptionId);
            response.put("message", "Subscription created successfully!");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "This subscription already exists");
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating subscription: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * API: Cancel subscription
     */
    @PostMapping("/api/cancel-subscription/{subscriptionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelSubscription(
            @PathVariable int subscriptionId,
            HttpSession session) {

        // ✅ Corrigé: utilise SESSION_USER_KEY
        UserSessionDTO userSessionDTO = (UserSessionDTO) session.getAttribute(SESSION_USER_KEY);

        if (userSessionDTO == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // ✅ Récupérer le seeker_id depuis JobSeekerProfile
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(userSessionDTO.getUserId());
            int seekerId = profile.getSeekerId();

            // Get subscription details before deleting
            List<Map<String, Object>> subscriptions = subscriptionService.getSubscriptionsBySeeker(seekerId);
            Map<String, Object> subscription = subscriptions.stream()
                    .filter(s -> s.get("subscription_id").equals(subscriptionId))
                    .findFirst()
                    .orElse(null);

            // Cancel subscription
            subscriptionService.cancelSubscription(subscriptionId);

            // Send cancellation email if subscription found
            if (subscription != null) {
                emailSuggestionService.sendCancellationEmail(
                        userSessionDTO.getEmail(),
                        userSessionDTO.getFullName(),
                        (String) subscription.get("skill_name"),
                        (String) subscription.get("province_name")
                );
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subscription cancelled successfully!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error cancelling subscription: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * API: Get subscription statistics
     */
    @GetMapping("/api/subscription-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSubscriptionStats(HttpSession session) {
        // ✅ Corrigé: utilise SESSION_USER_KEY
        UserSessionDTO userSessionDTO = (UserSessionDTO) session.getAttribute(SESSION_USER_KEY);

        if (userSessionDTO == null) {
            return ResponseEntity.status(401).build();
        }

        // ✅ Récupérer le seeker_id depuis JobSeekerProfile
        JobSeekerProfile2 profile = profileService.getOrCreateProfile(userSessionDTO.getUserId());
        int seekerId = profile.getSeekerId();

        Map<String, Object> stats = subscriptionService.getSubscriptionStats(seekerId);
        return ResponseEntity.ok(stats);
    }
}
