package com.joblink.joblink.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/service-packages")
public class EmployerServicePackageController {
    @GetMapping
    public String viewServicePackages(Model model){
        return "employer/service-packages";
    }
}
