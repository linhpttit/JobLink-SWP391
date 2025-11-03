package com.joblink.joblink.employer.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.joblink.joblink.employer.application.model.ApplicationsPageVM;
import com.joblink.joblink.employer.application.service.IEmployerApplicationService;

@Controller
@RequestMapping("/employer")
public class EmployerApplicationsController {

    private final IEmployerApplicationService service;

    public EmployerApplicationsController(IEmployerApplicationService service) {
        this.service = service;
    }

    @GetMapping("/applications")
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String date,      // today|week|month|"" (all)
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ){
        ApplicationsPageVM vm = service.getApplications(q, status, jobId, date, page, size);

        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("jobId", jobId);
        model.addAttribute("date", date);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        model.addAttribute("stats", vm.getStats());
        model.addAttribute("jobs", vm.getJobs());
        model.addAttribute("items", vm.getItems());
        model.addAttribute("meta", vm.getMeta());

        model.addAttribute("active", "applications"); 
        return "employer/applications";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String date
    ){
        String csv = service.exportCsv(q, status, jobId, date);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"applications.csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}
