package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.service.JobPostingService;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employer-dashboard")
public class EmployerDashboardController {
    private final JobPostingService jobPostingService;
    @GetMapping
    public String viewDashBoard(Model model, HttpSession session){
        List<JobPosting> jobs = jobPostingService.getAllJobPostings();
        long openJobsCount = jobPostingService.countOpenJobPosting();

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        String accountName = user.getFullName();

        System.out.println(accountName);
        model.addAttribute("accountName",accountName);
        model.addAttribute("openJobsCount",openJobsCount);
        model.addAttribute("jobs", jobs);
        return "employer/employer-dashboard";
    }
}
