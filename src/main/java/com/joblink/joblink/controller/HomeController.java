package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController implements ErrorController {

    private void putUser(Model model, HttpSession session) {
        model.addAttribute("user", session.getAttribute("user"));
    }

    private boolean ensureLogin(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        putUser(model, session);
        return "index"; // ✅ trang chủ
    }

    @GetMapping("/search")
    public String search(Model model, HttpSession session) {
        putUser(model, session);
        return "search";
    }

    @GetMapping("/seeker/home")
    public String seekerHome(HttpSession s, Model m) {
        if (s.getAttribute("user") == null) return "redirect:/signin";
        User u = (User) s.getAttribute("user");
        if (!"seeker".equalsIgnoreCase(u.getRole())) return "redirect:/signin";
        m.addAttribute("user", u);
        return "seeker-home";
    }


    @GetMapping("/employer/home")
    public String employerHome(Model model, HttpSession session) {
        if (!ensureLogin(session)) return "redirect:/signin";
        User u = (User) session.getAttribute("user");
        if (!"employer".equalsIgnoreCase(u.getRole())) return "redirect:/signin";
        putUser(model, session);
        return "employer-home";
    }
    @GetMapping("/employers")
    public String employers() {
        return "find-empoyers";
    }
    @GetMapping("/admin/home")
    public String adminHome(Model model, HttpSession session) {
        if (!ensureLogin(session)) return "redirect:/signin";
        User u = (User) session.getAttribute("user");
        if (!"admin".equalsIgnoreCase(u.getRole())) return "redirect:/signin";
        putUser(model, session);
        return "admin-home";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        return "error";
    }
}
