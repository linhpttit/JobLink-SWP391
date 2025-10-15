// File: JobDetailController.java (ĐÃ SỬA LỖI)
package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO; // Thêm import
import com.joblink.joblink.model.CVUpload; // Thêm import
import com.joblink.joblink.auth.model.EmployerProfile; // Sửa lại package model
import com.joblink.joblink.auth.model.JobPosting; // Sửa lại package model
import com.joblink.joblink.service.CVUploadService; // Thêm import
import com.joblink.joblink.service.EmployerService;
import com.joblink.joblink.service.JobService;
import com.joblink.joblink.service.ProfileService; // Thêm import
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor; // Thêm import
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor // SỬA LỖI 1: Tự động inject các service qua constructor
public class jobDetailController { // SỬA LỖI 2: Sửa lại tên class theo chuẩn Java

    // Inject các service cần thiết
    private final JobService jobService;
    private final EmployerService employerService;
    private final CVUploadService cvUploadService;
    private final ProfileService profileService; // Dùng để lấy seekerId từ userId

    @GetMapping("/job-detail/{jobId}")
    public String jobDetailPage(@PathVariable("jobId") int jobId, Model model, HttpSession session) {

        JobPosting job = jobService.getJobById(jobId);
        if (job == null) {
            return "error"; // Trả về trang lỗi chung
        }

        EmployerProfile employer = employerService.getProfileByEmployerId(job.getEmployerId());
        List<JobPosting> relatedJobs = jobService.getRelatedJobs(job.getCategoryId(), job.getJobId());

        UserSessionDTO currentUser = (UserSessionDTO) session.getAttribute("user");
        if (currentUser != null && "seeker".equalsIgnoreCase(currentUser.getRole())) {
            // SỬA LỖI 3: Lấy seekerId từ userId, sau đó mới dùng seekerId để lấy CV
            int seekerId = profileService.getOrCreateProfile(currentUser.getUserId()).getSeekerId();
            List<CVUpload> userCVs = cvUploadService.getCVsBySeekerId(seekerId);
            model.addAttribute("userCVs", userCVs);
        }

        model.addAttribute("job", job);
        model.addAttribute("employer", employer);
        model.addAttribute("relatedJobs", relatedJobs);

        return "job-detail";
    }
}