package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.model.ConnectionRequest;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.model.PremiumSubscription;
import com.joblink.joblink.service.ConnectionService;
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
@RequestMapping("/jobseeker/connections")
public class ConnectionController {
    private final ConnectionService connectionService;
    private final PremiumService premiumService;
    private final ProfileService profileService;

    public ConnectionController(ConnectionService connectionService,
                                PremiumService premiumService,
                                ProfileService profileService) {
        this.connectionService = connectionService;
        this.premiumService = premiumService;
        this.profileService = profileService;
    }

    /** ✅ Cho phép mọi seeker vào xem/gửi/accept lời mời (kể cả chưa premium) */
    @GetMapping
    public String showConnections(HttpSession session, Model model) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null || !"seeker".equalsIgnoreCase(user.getRole())) {
            return "redirect:/auth/login";
        }

        PremiumSubscription subscription = premiumService.getActiveSubscription(user.getUserId());
        boolean hasPremium = (subscription != null);

        JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
        if (profile == null) return "redirect:/jobseeker/profile";

        // Gợi ý (theo kỹ năng chung)
        List<Map<String, Object>> suggestions = connectionService.getSuggestedConnections(user.getUserId());
        // Lời mời đến & đã gửi
        List<ConnectionRequest> pendingRequests = connectionService.getPendingRequests(profile.getSeekerId());
        List<ConnectionRequest> sentRequests = connectionService.getSentRequests(profile.getSeekerId());

        model.addAttribute("suggestions", suggestions);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("sentRequests", sentRequests);
        model.addAttribute("user", user);
        model.addAttribute("hasPremium", hasPremium);

        return "connections";
    }

    @PostMapping("/request")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendRequest(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null || !"seeker".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            if (profile == null) {
                return ResponseEntity.status(400).body(Map.of("success", false, "error", "Vui lòng tạo hồ sơ JobSeeker trước"));
            }
            
            Object targetSeekerIdObj = payload.get("targetSeekerId");
            if (targetSeekerIdObj == null) {
                return ResponseEntity.status(400).body(Map.of("success", false, "error", "targetSeekerId is required"));
            }
            
            int targetSeekerId;
            if (targetSeekerIdObj instanceof Integer) {
                targetSeekerId = (Integer) targetSeekerIdObj;
            } else if (targetSeekerIdObj instanceof Number) {
                targetSeekerId = ((Number) targetSeekerIdObj).intValue();
            } else {
                targetSeekerId = Integer.parseInt(targetSeekerIdObj.toString());
            }
            
            String message = (String) payload.getOrDefault("message", "");
            var request = connectionService.sendConnectionRequest(profile.getSeekerId(), targetSeekerId, message);
            return ResponseEntity.ok(Map.of("success", true, "requestId", request.getRequestId()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @PostMapping("/accept/{requestId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> acceptRequest(@PathVariable int requestId, HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null || !"seeker".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }
        try {
            connectionService.acceptConnectionRequest(requestId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @PostMapping("/reject/{requestId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectRequest(@PathVariable int requestId, HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null || !"seeker".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }
        try {
            connectionService.rejectConnectionRequest(requestId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @GetMapping("/suggestions")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getSuggestions(HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null || !"seeker".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(connectionService.getSuggestedConnections(user.getUserId()));
    }
}
