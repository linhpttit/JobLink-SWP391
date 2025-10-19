package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO; // ✅ IMPORT ĐÚNG
import com.joblink.joblink.model.CVUpload;
import com.joblink.joblink.model.JobSeekerProfile;
import com.joblink.joblink.service.CVUploadService;
import com.joblink.joblink.service.FileUploadService;
import com.joblink.joblink.service.ProfileService;
import com.joblink.joblink.util.Constants;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/jobseeker")
public class CVController {

    private final CVUploadService cvUploadService;
    private final FileUploadService fileUploadService;
    private final ProfileService profileService;

    public CVController(CVUploadService cvUploadService,
                        FileUploadService fileUploadService,
                        ProfileService profileService) {
        this.cvUploadService = cvUploadService;
        this.fileUploadService = fileUploadService;
        this.profileService = profileService;
    }

    @GetMapping("/cv")
    public String cvUploadPage(HttpSession session, Model model, RedirectAttributes ra) {
        // ✅ SỬ DỤNG UserSessionDTO
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Please login to continue");
            return "redirect:/signin";
        }

        if (!"seeker".equalsIgnoreCase(user.getRole())) {
            ra.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        // Get profile to auto-fill basic information
        JobSeekerProfile profile = profileService.getOrCreateProfile(user.getUserId());

        // Get recent CV uploads (last 5)
        List<CVUpload> recentCVs = cvUploadService.getRecentCVs(profile.getSeekerId(), 5);

        model.addAttribute("profile", profile);
        model.addAttribute("recentCVs", recentCVs);
        model.addAttribute("vietnameseCities", Constants.VIETNAMESE_CITIES);
        model.addAttribute("jobLevels", Constants.JOB_LEVELS);
        model.addAttribute("workModes", Constants.WORK_MODES);
        model.addAttribute("salaryRanges", Constants.SALARY_RANGES);

        return "cv-upload";
    }

    @PostMapping("/cv/upload")
    public String uploadCV(
            @RequestParam("cvFile") MultipartFile cvFile,
            @RequestParam String fullName,
            @RequestParam String phoneNumber,
            @RequestParam String email,
            @RequestParam String preferredLocation,
            @RequestParam Integer yearsOfExperience,
            @RequestParam String currentJobLevel,
            @RequestParam String workMode,
            @RequestParam String expectedSalary,
            @RequestParam(required = false) String currentSalary,
            @RequestParam(required = false) String coverLetter,
            HttpSession session,
            RedirectAttributes ra) {

        // ✅ ĐỔI THÀNH UserSessionDTO
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Please login to continue");
            return "redirect:/signin";
        }

        try {
            // Upload CV file
            String cvFileUrl = fileUploadService.uploadCV(cvFile);

            // Get profile
            JobSeekerProfile profile = profileService.getOrCreateProfile(user.getUserId());

            // Create CV upload record
            CVUpload cv = new CVUpload();
            cv.setSeekerId(profile.getSeekerId());
            cv.setFullName(fullName);
            cv.setPhoneNumber(phoneNumber);
            cv.setEmail(email);
            cv.setPreferredLocation(preferredLocation);
            cv.setYearsOfExperience(yearsOfExperience);
            cv.setCurrentJobLevel(currentJobLevel);
            cv.setWorkMode(workMode);
            cv.setExpectedSalary(expectedSalary);
            cv.setCurrentSalary(currentSalary);
            cv.setCoverLetter(coverLetter);
            cv.setCvFileUrl(cvFileUrl);
            cv.setCvFileName(cvFile.getOriginalFilename());

            cvUploadService.saveCV(cv);

            ra.addFlashAttribute("success", "CV uploaded successfully!");
            return "redirect:/jobseeker/cv";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error uploading CV: " + e.getMessage());
            return "redirect:/jobseeker/cv";
        }
    }
}