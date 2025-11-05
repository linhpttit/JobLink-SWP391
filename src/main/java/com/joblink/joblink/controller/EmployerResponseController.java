package com.joblink.joblink.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employer-response")
public class EmployerResponseController {
    @GetMapping
    public String viewComplaintPage(Model model){
        return "employer/employer-response";
    }
}
