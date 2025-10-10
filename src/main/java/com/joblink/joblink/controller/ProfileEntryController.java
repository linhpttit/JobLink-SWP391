// src/main/java/com/joblink/joblink/controller/ProfileEntryController.java
package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileEntryController {

    @GetMapping("/profile")
    public String profileRoot(HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u == null) return "redirect:/signin";

        String r = u.getRole() == null ? "" : u.getRole().toLowerCase();
        return switch (r) {
            case "seeker"   -> "redirect:/jobseeker/profile";
            case "employer" -> "redirect:/employer/profile"; // để sau triển khai
            case "admin"    -> "redirect:/admin/profile";    // để sau triển khai
            default         -> "redirect:/signin";
        };
    }
}
