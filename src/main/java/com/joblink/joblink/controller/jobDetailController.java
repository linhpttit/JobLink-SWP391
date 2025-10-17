package com.joblink.joblink.controller;

import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class jobDetailController {

    private final JobService jobService;

    @GetMapping("/job-detail/{jobId}")
    public String jobDetailPage(@PathVariable("jobId") Long jobId, Model model) {

        try {
            // Lấy job từ database
            Optional<JobPosting> jobOptional = jobService.getJobById(jobId);

            if (jobOptional.isPresent()) {
                JobPosting job = jobOptional.get();
                model.addAttribute("job", job);
            } else {
                // Nếu không tìm thấy, vẫn hiển thị trang với dữ liệu fix cứng
                model.addAttribute("job", null);
            }

            return "job-detail";

        } catch (Exception e) {
            System.err.println("Error loading job detail: " + e.getMessage());
            e.printStackTrace();
            // Trả về trang với dữ liệu fix cứng nếu có lỗi
            model.addAttribute("job", null);
            return "job-detail";
        }
    }
}