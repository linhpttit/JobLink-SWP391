package com.joblink.joblink.controller;

// domain User import removed; session holds UserSessionDTO now
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private boolean ensureAdmin(HttpSession session) {
        com.joblink.joblink.dto.UserSessionDTO u = (com.joblink.joblink.dto.UserSessionDTO) session.getAttribute("user");
        return u != null && "admin".equalsIgnoreCase(u.getRole());
    }

    private void putUser(Model model, HttpSession session) {
        model.addAttribute("user", session.getAttribute("user"));
    }

    @GetMapping({"", "/"})
    public String adminShell(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "admin";
    }

  
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "dashboard";
    }

    @GetMapping("/jobseeker")
    public String jobseeker(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "jobseeker";
    }

    @GetMapping("/recruitment")
    public String recruitment(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "recruiment"; // template file is recruiment.html in templates
    }

    @GetMapping("/employer")
    public String employer(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "employer";
    }

    @GetMapping("/applications")
    public String applications(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "applications";
    }

    @GetMapping("/companies")
    public String companies(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "companies";
    }

    @GetMapping("/blog")
    public String blog(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "blog";
    }

    @GetMapping("/premium")
    public String premium(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "premium";
    }

    @GetMapping("/payments")
    public String payments(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "payment";
    }

    @GetMapping("/feedbacks")
    public String feedbacks(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "feedbacks";
    }

    @GetMapping("/statistic")
    public String statistic(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "statistic";
    }
}
