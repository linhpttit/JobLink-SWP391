package com.joblink.joblink.employer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.joblink.joblink.employer.application.model.JobPostingForm;
import com.joblink.joblink.employer.application.service.DistrictService;
import com.joblink.joblink.employer.application.service.EmployerService;
import com.joblink.joblink.employer.application.service.JobPostingService;
import com.joblink.joblink.employer.application.service.ProvinceService;
import com.joblink.joblink.employer.application.service.SkillService;

import jakarta.validation.Valid;

@Controller("NewJobPostingController")
@RequestMapping("/employer/job-postings")
public class JobPostingController {

	private final JobPostingService jobPostingService;
    private final SkillService skillService;
    private final ProvinceService provinceService;
    private final DistrictService districtService;
    private final EmployerService employerService;

    public JobPostingController(JobPostingService jobPostingService,
                                SkillService skillService,
                                ProvinceService provinceService,
                                DistrictService districtService,
                                EmployerService employerService) {
        this.jobPostingService = jobPostingService;
        this.skillService = skillService;
        this.provinceService = provinceService;
        this.districtService = districtService;
        this.employerService = employerService;
    }

    @ModelAttribute("skills")
    public Object skills(){ return skillService.findAll(); }

    @ModelAttribute("provinces")
    public Object provinces(){ return provinceService.findAll(); }

    @ModelAttribute("districts")
    public Object districts(){ return districtService.findAll(); }

    @GetMapping
    public String openCreatePage(Model model) {
        model.addAttribute("active", "postings");
        if (!model.containsAttribute("jobForm")) {
            JobPostingForm form = new JobPostingForm();
            try { form.setEmployerId(employerService.getCurrentEmployerId()); } catch (Exception ignored) {}
            model.addAttribute("jobForm", form);
        }
        return "employer/job-post";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("jobForm") JobPostingForm form,
                         BindingResult binding,
                         RedirectAttributes ra) {
        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.jobForm", binding);
            ra.addFlashAttribute("jobForm", form);
            return "redirect:/jobPosting";
        }
        try {
            jobPostingService.create(form);
            ra.addFlashAttribute("success", "Tạo tin tuyển dụng thành công!");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("jobForm", form);
        }
        return "redirect:/jobPosting";
    }
}
