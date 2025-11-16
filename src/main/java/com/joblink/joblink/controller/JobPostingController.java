package com.joblink.joblink.controller;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.dto.JobPostingDto;
import com.joblink.joblink.entity.District;
import com.joblink.joblink.entity.Province;
import com.joblink.joblink.entity.Skill;
import com.joblink.joblink.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobPosting")
@RequiredArgsConstructor
public class JobPostingController {
    private final JdbcTemplate jdbc;
    private final IJobPostingService jobPostingService;
    private final ISkillService skillService;
    private final IProvinceService provinceService;
    private final IDistrictService districtService;
    private final PaymentService paymentService;
    @GetMapping
    public String showCreateForm(Model model, HttpSession session, RedirectAttributes ra){
        // Kiểm tra login
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            return "redirect:/signin";
        }

        com.joblink.joblink.dto.UserSessionDTO user =
                (com.joblink.joblink.dto.UserSessionDTO) userObj;

        // Kiểm tra tier và số lượng bài đăng
        try {
            Map<String, Object> tierInfo = paymentService.getEmployerTierInfo(user.getUserId());
            Integer currentTier = (Integer) tierInfo.get("tierLevel");
            Integer currentPosts = (Integer) tierInfo.get("currentJobPosts");
            Integer maxPosts = (Integer) tierInfo.get("maxJobPosts");

            // Kiểm tra đã đạt giới hạn chưa (chỉ áp dụng cho tier < 3)
            if (currentTier < 3 && currentPosts >= maxPosts) {
                ra.addFlashAttribute("error",
                        "Bạn đã đạt giới hạn " + maxPosts + " bài đăng của gói " + getTierName(currentTier) +
                                ". Vui lòng nâng cấp gói để đăng thêm bài!");
                ra.addFlashAttribute("upgradeLink", "/payment/upgrade");
                return "redirect:/jobPosting/viewList";
            }

            // Thêm thông tin tier vào model
            model.addAttribute("currentTier", currentTier);
            model.addAttribute("currentPosts", currentPosts);
            model.addAttribute("maxPosts", maxPosts);
            model.addAttribute("isUnlimited", currentTier >= 3);
            model.addAttribute("maxPostsDisplay", currentTier >= 3 ? "Không giới hạn" : maxPosts);
            model.addAttribute("remainingPosts", maxPosts - currentPosts);
            model.addAttribute("remainingPostsDisplay", currentTier >= 3 ? "Không giới hạn" : (maxPosts - currentPosts));

        } catch (Exception e) {
            System.err.println("Error checking tier: " + e.getMessage());
        }
        model.addAttribute("jobForm", new JobPostingDto());
        model.addAttribute("skills", skillService.getAllSkills());
        model.addAttribute("provinces", provinceService.getAllProvinces());
        model.addAttribute("districts", districtService.getAllDistricts());
        return "employer/job-post";
    }

    @PostMapping
    public String createJobPosting(
            @ModelAttribute("jobForm") JobPostingDto dto,
            BindingResult result,
            Model model,
            HttpSession session,
        RedirectAttributes ra){
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            return "redirect:/signin";
        }

        com.joblink.joblink.dto.UserSessionDTO user =
                (com.joblink.joblink.dto.UserSessionDTO) userObj;
        Integer userId = user.getUserId();
        Integer employerId = jdbc.queryForObject(
                "SELECT employer_id FROM EmployerProfile WHERE user_id = ?",
                Integer.class,
                user.getUserId()
        );
        try {
            Map<String, Object> tierInfo = paymentService.getEmployerTierInfo(user.getUserId());
            Integer currentTier = (Integer) tierInfo.get("tierLevel");
            Integer currentPosts = (Integer) tierInfo.get("currentJobPosts");
            Integer maxPosts = (Integer) tierInfo.get("maxJobPosts");

            // Chỉ kiểm tra giới hạn cho tier < 3
            if (currentTier < 3 && currentPosts >= maxPosts) {
                ra.addFlashAttribute("error",
                        "Bạn đã đạt giới hạn " + maxPosts + " bài đăng. Vui lòng nâng cấp gói!");
                return "redirect:/jobPosting/viewList";
            }
        } catch (Exception e) {
            System.err.println("Error checking tier: " + e.getMessage());
        }
        if (result.hasErrors()) {
            // CÓ LỖI:
            // Phải tải lại toàn bộ dữ liệu cần thiết cho các dropdown list của form
            // vì khi trả về, trang "job-post" sẽ cần các dữ liệu này để hiển thị.
            System.out.println("Validation errors found: " + result.getAllErrors()); // Dòng này để debug, có thể xóa

            // Đưa dữ liệu trở lại Model để Thymeleaf có thể render trang
            model.addAttribute("skills", skillService.getAllSkills());
            model.addAttribute("provinces", provinceService.getAllProvinces());
            model.addAttribute("districts", districtService.getAllDistricts());

            // Trả về lại trang form để người dùng sửa lỗi
            return "employer/job-post";
        }
        jobPostingService.createJobPosting(dto,employerId);
        return "redirect:/jobPosting/viewList";
    }

    @GetMapping("/viewList")
    public String showJobPostingList(Model model) {
        List<JobPosting> jobPostings = jobPostingService.getAllJobPostings();
        model.addAttribute("jobs", jobPostings);
        return "employer/job-list-view";
    }

    @GetMapping("/detail/{id}")
    public String showDetailPosting(@PathVariable("id") Long id, Model model){
        JobPosting jobPosting = jobPostingService.findJobPostingById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        model.addAttribute("job",jobPosting);
        return "employer/job-detail";
    }
    @PostMapping("/{id}/delete")
    public String deleteJobPosting(@PathVariable("id") Long id){
        jobPostingService.deleteJobPostingById(id);
        return "redirect:/jobPosting/viewList";
    }

    @GetMapping("/{id}/edit")
    public String editJobPosting(@PathVariable("id") Long id, Model model){
        JobPosting jobPosting = jobPostingService.findJobPostingById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        model.addAttribute("job",jobPosting);

        // Bổ sung danh sách để hiển thị trong dropdown
        model.addAttribute("skills", skillService.getAllSkills());
        model.addAttribute("provinces", provinceService.getAllProvinces());
        model.addAttribute("districts", districtService.getAllDistricts());
        return "employer/edit-job-posting";
    }

    @PostMapping("/{id}/edit")
    public String editJobPosting(@PathVariable("id") Long id,
                                 JobPosting posting,
                                 Model model){
        jobPostingService.editJobPostingByEntity(id, posting);
        return "redirect:/jobPosting/viewList";
    }

    private String getTierName(Integer tierLevel) {
        return switch (tierLevel) {
            case 0 -> "Free";
            case 1 -> "Basic";
            case 2 -> "Premium";
            case 3 -> "Enterprise";
            default -> "Unknown";
        };
    }
}
