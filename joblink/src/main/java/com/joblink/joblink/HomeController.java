package com.joblink.joblink;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
    @GetMapping("/signin")
    public String signin() {
        return "signin";
    }
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
    @GetMapping("/verification")
    public String verification() {
        return "verification";
    }

}