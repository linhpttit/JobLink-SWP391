package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.EmployerProfile;
import com.joblink.joblink.service.EmployerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employers")
@RequiredArgsConstructor
public class EmployerController {
    private final EmployerService employerService;

    @GetMapping("/open")
    public ResponseEntity<List<EmployerProfile>> searchOpenEmployers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String industry,
            @RequestParam(defaultValue = "most_jobs") String sort
    ) {
        return ResponseEntity.ok(
                employerService.searchOpenEmployers(keyword, location, industry, sort)
        );
    }
}

