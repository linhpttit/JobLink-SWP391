package com.joblink.joblink.employer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.joblink.joblink.employer.application.service.IServicePackageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/employer")
@RequiredArgsConstructor
public class ServicePackageController {
	private final IServicePackageService service;

    @GetMapping("/service-packages")
    public String list(Model model) {
        model.addAttribute("packages", service.listEmployerPackages());
        return "employer/service-packages";
    }
}
