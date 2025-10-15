// File: com/joblink/joblink/controller/jobDetailController.java (PHIÊN BẢN SỬA LỖI HOÀN CHỈNH)
package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.model.CVUpload;
import com.joblink.joblink.entity.Employer; // Sử dụng Employer entity
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.service.CVUploadService;
import com.joblink.joblink.service.EmployerService; // Giả sử service này trả về Employer
import com.joblink.joblink.service.JobService;
import com.joblink.joblink.service.ProfileService;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class jobDetailController {

    private final JobService jobService;
    private final EmployerService employerService;
    private final CVUploadService cvUploadService;
    private final ProfileService profileService;

    @GetMapping("/job-detail/{jobId}")
    @Transactional(readOnly = true)
    public String jobDetailPage(@PathVariable("jobId") Long jobId, Model model, HttpSession session) {

        // Bước 1: Lấy JobPosting từ service, nó trả về Optional
        Optional<JobPosting> jobOptional = jobService.getJobById(jobId);
        if (jobOptional.isEmpty()) {
            return "error"; // Không tìm thấy Job, trả về trang lỗi
        }
        JobPosting job = jobOptional.get();

        // Bước 2: SỬA LỖI - Lấy đối tượng Employer trực tiếp từ JobPosting
        Employer employer = job.getEmployer();
        if (employer == null) {
            return "error"; // Lỗi dữ liệu: Job không có thông tin nhà tuyển dụng
        }

        // Bước 3: SỬA LỖI - Lấy các job liên quan qua Category ID
        List<JobPosting> relatedJobs = Collections.emptyList();
        if (job.getCategory() != null) {
            relatedJobs = jobService.getRelatedJobs(job.getCategory().getCategoryId().intValue(), job.getJobId());
        }

        // Bước 4: Lấy thông tin CV của người dùng (nếu đang đăng nhập là seeker)
        UserSessionDTO currentUser = (UserSessionDTO) session.getAttribute("user");
        if (currentUser != null && "seeker".equalsIgnoreCase(currentUser.getRole())) {
            int seekerId = profileService.getOrCreateProfile(currentUser.getUserId()).getSeekerId();
            List<CVUpload> userCVs = cvUploadService.getCVsBySeekerId(seekerId);
            model.addAttribute("userCVs", userCVs);
        }

        // Bước 5: Đưa tất cả dữ liệu vào model
        model.addAttribute("job", job);
        model.addAttribute("employer", employer); // Gửi cả đối tượng Employer
        model.addAttribute("relatedJobs", relatedJobs);

        return "job-detail";
    }
}