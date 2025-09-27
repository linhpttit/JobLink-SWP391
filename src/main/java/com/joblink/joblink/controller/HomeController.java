package com.joblink.joblink.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import com.joblink.joblink.auth.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController implements ErrorController {
    private boolean ensureLogin(HttpSession s) { return s.getAttribute("user") != null; }
    private void addUser(Model m, HttpSession s) { m.addAttribute("user", (User) s.getAttribute("user")); }
    private void putUser(Model m, HttpSession s) {
        m.addAttribute("user",(User) s.getAttribute("user"));
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }



    @GetMapping("/search")
    public String search(){
        return "search";
    }


    @GetMapping("/admin/home")
    public String adminHome(HttpSession s, Model m) {
        if (!ensureLogin(s)) return "redirect:/login";
        User u = (User) s.getAttribute("user");
        if(!"admin".equalsIgnoreCase(u.getRole())) return "redirect:/login";
        putUser(m,s);
        return "admin-home";
    }

    @GetMapping("/employer/home")
    public String employerHome(HttpSession s, Model m) {
        if (!ensureLogin(s)) return "redirect:/login";
        User u = (User) s.getAttribute("user");
        if (!"employer".equalsIgnoreCase(u.getRole())) return "redirect:/login";
        putUser(m, s);

        return "employer-home";
    }

    @GetMapping("/seeker/home")
    public String seekerHome(HttpSession s, Model m) {
        if (!ensureLogin(s)) return "redirect:/login";
        User u = (User) s.getAttribute("user");
        if (!"seeker".equalsIgnoreCase(u.getRole())) return "redirect:/login";
        putUser(m, s);
        return "seeker-home";
    }

    // Xử lý trang lỗi 404
//    @GetMapping("/404")
//    public String error404() {
//        return "404"; // Trả về file 404.html
//    }

    // Xử lý tất cả lỗi
//    @RequestMapping("/error")
//    public String handleError(HttpServletRequest request) {
//        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
//
//        if (status != null) {
//            Integer statusCode = Integer.valueOf(status.toString());
//
//            if (statusCode == 404) {
//                return "error"; // Trả về trang 404.html
//            }
//            else if (statusCode == 500) {
//                return "error"; // Có thể tạo trang 500.html cho lỗi server
//            }
//        }
//
//        return "error"; // Mặc định trả về 404
//    }
}