package com.joblink.joblink.employer.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.joblink.joblink.employer.application.service.IEmployerReportService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/employer")
@RequiredArgsConstructor
public class EmployerHomeController {

    private final IEmployerReportService reportService;


    @GetMapping
    public String employerRoot() {
        return "redirect:/employer/dashboard";
    }

    @GetMapping("/dashboard")
    public String employerHome(
            @RequestParam(required = false)
            @jakarta.validation.constraints.Pattern(regexp = "7d|30d|90d|custom", message = "range không hợp lệ")
            String range,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long jobId,
            Model model
    ) {
        model.addAttribute("active", "overview");
        var vm = reportService.getDashboard(range, from, to, jobId);

        model.addAttribute("range", vm.getRange());
        model.addAttribute("from", vm.getFrom());
        model.addAttribute("to", vm.getTo());
        model.addAttribute("jobId", vm.getJobId());
        model.addAttribute("kpi", vm.getKpi());
        model.addAttribute("jobs", vm.getJobs());
        model.addAttribute("topJobs", vm.getTopJobs());
        model.addAttribute("deadlines", vm.getDeadlines());
        model.addAttribute("recentApplicants", vm.getRecentApplicants());
        model.addAttribute("chart", vm.getChart());

        return "employer/dashboard"; 
    }
}
