package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.*;
import com.joblink.joblink.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/jobseeker") // đường dẫn là /jobseeker/profile
public class ProfileController {

    private final ProfileService profileService;
    private final HttpSession session;

    public ProfileController(ProfileService profileService, HttpSession session) {
        this.profileService = profileService;
        this.session = session;
    }

    private User current() {
        return (User) session.getAttribute("user");
    }
    // lấy user từ session hiện tại

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        User u = current();
        if (u == null) return "redirect:/signin";

        JobSeekerProfile p = profileService.getOrInitProfile(u.getUserId());

        var avatarMap = (java.util.Map<Integer, String>) session.getAttribute("__avatar_map__");
        if (avatarMap != null && avatarMap.containsKey(u.getUserId())) {
            p.setAvatarUrl(avatarMap.get(u.getUserId()));
        }
        //lấy avatar

        ProfileCompletion completion = profileService.computeCompletion(u.getUserId());

        model.addAttribute("profile", p);
        model.addAttribute("completion", completion);
        model.addAttribute("user", u);
        return "seeker-profile";

    }

    @PostMapping("/profile/basic")
    public String updateBasic(@ModelAttribute BasicInfoUpdate req) {
        User u = current();
        if (u == null) return "redirect:/signin";

        profileService.updateBasicInfo(u.getUserId(), req);

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            u.setFullName(req.getFullName().trim());
            session.setAttribute("user", u);
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            u.setEmail(req.getEmail().trim());
            session.setAttribute("user", u);
        }
        return "redirect:/jobseeker/profile";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam("url") String avatarUrl) {
        User u = current();
        if (u == null) return "redirect:/signin";

        profileService.updateAvatarInSession(u.getUserId(), avatarUrl);
        System.out.println("[ProfileController] Avatar updated for " + u.getEmail());
        return "redirect:/jobseeker/profile";
    }

}
