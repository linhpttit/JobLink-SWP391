package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.model.CVUpload;
import com.joblink.joblink.model.JobSeekerProfile;
import com.joblink.joblink.service.CVUploadService;
import com.joblink.joblink.service.JobService;
import com.joblink.joblink.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class jobDetailController {

    private final JobService jobService;
    private final CVUploadService cvUploadService;
    private final ProfileService profileService;

    @GetMapping("/job-detail/{jobId}")
    public String jobDetail(@PathVariable long jobId,
                            HttpSession session,
                            Model model,
                            RedirectAttributes ra) {
        try {
            // Lấy thông tin job
            Optional<JobPosting> jobOptional = jobService.getJobById(jobId);

            // ✅ SỬA LỖI: Kiểm tra Optional và lấy giá trị thực
            if (!jobOptional.isPresent()) {
                ra.addFlashAttribute("error", "Công việc không tồn tại");
                return "redirect:/search";
            }

            JobPosting job = jobOptional.get(); // Lấy JobPosting từ Optional
            model.addAttribute("job", job);

            // Nếu user đã login, lấy danh sách CV đã upload
            UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
            if (user != null && "seeker".equalsIgnoreCase(user.getRole())) {
                JobSeekerProfile profile = profileService.getOrCreateProfile(user.getUserId());
                List<CVUpload> userCVs = cvUploadService.getAllCVsBySeeker(profile.getSeekerId());
                model.addAttribute("userCVs", userCVs);
                model.addAttribute("isLoggedIn", true);
            } else {
                model.addAttribute("isLoggedIn", false);
            }

            return "job-detail";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi tải thông tin công việc");
            return "redirect:/search";
        }
    }

    @PostMapping("/job/apply")
    @ResponseBody
    public Map<String, Object> applyJob(@RequestParam int jobId,
                                        @RequestParam int cvId,
                                        @RequestParam(required = false) String coverLetter,
                                        HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để ứng tuyển");
            response.put("redirect", "/signin");
            return response;
        }

        if (!"seeker".equalsIgnoreCase(user.getRole())) {
            response.put("success", false);
            response.put("message", "Chỉ ứng viên mới có thể ứng tuyển");
            return response;
        }

        try {
            JobSeekerProfile profile = profileService.getOrCreateProfile(user.getUserId());

            // Kiểm tra đã ứng tuyển chưa
            boolean alreadyApplied = jobService.hasApplied(jobId, profile.getSeekerId());
            if (alreadyApplied) {
                response.put("success", false);
                response.put("message", "Bạn đã ứng tuyển công việc này rồi");
                return response;
            }

            // Lấy thông tin CV
            CVUpload cv = cvUploadService.getCVById(cvId);
            if (cv == null || cv.getSeekerId() != profile.getSeekerId()) {
                response.put("success", false);
                response.put("message", "CV không hợp lệ");
                return response;
            }

            // Tạo application
            jobService.applyJob(jobId, profile.getSeekerId(), cv.getCvFileUrl(), coverLetter);

            response.put("success", true);
            response.put("message", "Ứng tuyển thành công!");
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }
}