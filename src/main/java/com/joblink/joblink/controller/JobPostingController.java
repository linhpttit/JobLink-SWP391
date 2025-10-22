package com.joblink.joblink.controller;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.dto.JobPostingDto;
import com.joblink.joblink.entity.District;
import com.joblink.joblink.entity.Province;
import com.joblink.joblink.entity.Skill;
import com.joblink.joblink.service.IDistrictService;
import com.joblink.joblink.service.IJobPostingService;
import com.joblink.joblink.service.IProvinceService;
import com.joblink.joblink.service.ISkillService;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jobPosting")
@RequiredArgsConstructor
public class JobPostingController {
    private final IJobPostingService jobPostingService;
    private final ISkillService skillService;
    private final IProvinceService provinceService;
    private final IDistrictService districtService;

    @GetMapping
    public String showList(){
        return "redirect:/jobPosting/viewList";
    }
    @GetMapping("/viewList")
    public String showJobPostingList(Model model) {
        List<JobPosting> jobPostings = jobPostingService.getAllJobPostings();
        model.addAttribute("jobs", jobPostings);
        return "employer/job-list-view";
    }

    @GetMapping("/showCreateForm")
    public String showCreateForm(Model model){
        model.addAttribute("jobForm", new JobPostingDto());
        // ✅ Dữ liệu giả để test (sau này lấy từ DB)
        List<Skill> skills = List.of(
                new Skill(1L, "Java"),
                new Skill(2L, "Spring Boot"),
                new Skill(3L, "React"),
                new Skill(4L, "SQL")
        );

        List<Province> provinces = List.of(
                new Province(1L, "Hà Nội", "HN"),
                new Province(2L, "TP Hồ Chí Minh", "HCM")
        );

        List<District> districts = List.of(
                new District(1L, "Quận 1", 1L),
                new District(2L, "Quận 3", 1L),
                new District(3L, "Ba Đình", 1L)
        );

        model.addAttribute("skills", skills);
        model.addAttribute("provinces", provinces);
        model.addAttribute("districts", districts);
        return "employer/job-post";
    }

    @PostMapping
    public String createJobPosting(
            @ModelAttribute("jobForm") JobPostingDto dto, Long employerId,
            BindingResult result,
            Model model){
        if (result.hasErrors()) {
            // CÓ LỖI:
            // Phải tải lại toàn bộ dữ liệu cần thiết cho các dropdown list của form
            // vì khi trả về, trang "job-post" sẽ cần các dữ liệu này để hiển thị.
            System.out.println("Validation errors found: " + result.getAllErrors()); // Dòng này để debug, có thể xóa

            List<Skill> skills = List.of(
                    new Skill(1L, "Java"),
                    new Skill(2L, "Spring Boot"),
                    new Skill(3L, "React"),
                    new Skill(4L, "SQL")
            );

            List<Province> provinces = List.of(
                    new Province(1L, "Hà Nội", "HN"),
                    new Province(2L, "TP Hồ Chí Minh", "HCM")
            );

            List<District> districts = List.of(
                    new District(1L, "Quận 1", 1L),
                    new District(2L, "Quận 3", 1L),
                    new District(3L, "Ba Đình", 1L)
            );

            // Đưa dữ liệu trở lại Model để Thymeleaf có thể render trang
            model.addAttribute("skills", skills);
            model.addAttribute("provinces", provinces);
            model.addAttribute("districts", districts);

            // Trả về lại trang form để người dùng sửa lỗi
            return "employer/job-post";
        }
        jobPostingService.createJobPosting(dto);
        return "redirect:/jobPosting/viewList";
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

}
