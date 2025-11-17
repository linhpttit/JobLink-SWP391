package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.model.CVUpload;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.service.CVUploadService;
import com.joblink.joblink.service.JobBookmarkService;
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
    private final JobBookmarkService jobBookmarkService;

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
                JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
                List<CVUpload> userCVs = cvUploadService.getAllCVsBySeeker(profile.getSeekerId());
                model.addAttribute("userCVs", userCVs);
                model.addAttribute("isLoggedIn", true);
                boolean bookmarked = jobBookmarkService.isBookmarked(profile.getSeekerId(), jobId);
                model.addAttribute("isBookmarked", bookmarked);
            } else {
                model.addAttribute("isLoggedIn", false);
                model.addAttribute("isBookmarked", false);
            }

            return "job-detail";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi tải thông tin công việc");
            return "redirect:/search";
        }
    }

    @PostMapping("/job/bookmark")
    @ResponseBody
    public Map<String, Object> toggleBookmark(@RequestParam long jobId,
                                              @RequestParam String action,
                                              HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập");
            res.put("redirect", "/signin?redirect=/job-detail/" + jobId);
            return res;
        }
        if (!"seeker".equalsIgnoreCase(user.getRole())) {
            res.put("success", false);
            res.put("message", "Chỉ ứng viên mới được lưu công việc");
            return res;
        }
        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            if ("add".equalsIgnoreCase(action)) {
                jobBookmarkService.addBookmark(profile.getSeekerId(), jobId);
            } else if ("remove".equalsIgnoreCase(action)) {
                jobBookmarkService.removeBookmark(profile.getSeekerId(), jobId);
            } else {
                res.put("success", false);
                res.put("message", "Hành động không hợp lệ");
                return res;
            }
            res.put("success", true);
            return res;
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Lỗi: " + e.getMessage());
            return res;
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
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());

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