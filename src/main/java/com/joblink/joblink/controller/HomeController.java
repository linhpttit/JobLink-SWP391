package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.service.HomeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController implements ErrorController {

    @Autowired
    private HomeService homeService;

    private void putUser(Model model, HttpSession session) {
        model.addAttribute("user", session.getAttribute("user"));
    }

    private boolean ensureLogin(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // If user logged in, redirect to area by role
        Object obj = session.getAttribute("user");
        if (obj instanceof UserSessionDTO) {
            UserSessionDTO u = (UserSessionDTO) obj;
            String role = u.getRole() == null ? "" : u.getRole().toLowerCase();
            switch (role) {
                case "admin":
                    return "redirect:/admin";
                case "employer":
                    return "redirect:/employer/employer-layout";
                case "seeker":
                    return "redirect:/seeker/home";
                default:
                    break;
            }
        }

        putUser(model, session);

        // Load dynamic data for homepage
        model.addAttribute("stats", homeService.getHomeStats());
        model.addAttribute("categories", homeService.getPopularCategories());
        model.addAttribute("featuredJobs", homeService.getFeaturedJobs());
        model.addAttribute("topCompanies", homeService.getTopCompanies());

        return "index";
    }

    @GetMapping("/search")
    public String search(Model model, HttpSession session) {
        putUser(model, session);
        return "search";
    }

    @GetMapping("/seeker/home")
    public String seekerHome(HttpSession s, Model m) {
        if (s.getAttribute("user") == null) return "redirect:/signin";
        UserSessionDTO u = (UserSessionDTO) s.getAttribute("user");
        if (!"seeker".equalsIgnoreCase(u.getRole())) return "redirect:/signin";
        m.addAttribute("user", u);

        // Load dynamic data for seeker home
        m.addAttribute("stats", homeService.getHomeStats());
        m.addAttribute("categories", homeService.getPopularCategories());
        m.addAttribute("featuredJobs", homeService.getFeaturedJobs());
        m.addAttribute("topCompanies", homeService.getTopCompanies());

        return "seeker-home";
    }

    @GetMapping("/employer/home")
    public String employerHome(Model model, HttpSession session) {
        if (!ensureLogin(session)) return "redirect:/signin";
        UserSessionDTO u = (UserSessionDTO) session.getAttribute("user");
        if (!"employer".equalsIgnoreCase(u.getRole())) return "redirect:/signin";
        putUser(model, session);
        return "employer/employer-layout";
    }

    @GetMapping("/employers")
    public String employers() {
        return "find-empoyers";
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model, HttpSession session) {
        if (!ensureLogin(session)) return "redirect:/signin";
        UserSessionDTO u = (UserSessionDTO) session.getAttribute("user");
        if (!"admin".equalsIgnoreCase(u.getRole())) return "redirect:/signin";
        return "redirect:/admin";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        return "error";
    }
}