package com.joblink.joblink.controller;

// domain User is not stored in session anymore; use UserSessionDTO
import com.joblink.joblink.dto.UserSessionDTO; // New import for UserSessionDTO
import com.joblink.joblink.model.*;
import com.joblink.joblink.service.FileUploadService;
import com.joblink.joblink.service.ProfileService;
import com.joblink.joblink.util.Constants;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobseeker")
public class ProfileController {

    private final ProfileService profileService;
    private final FileUploadService fileUploadService;

    public ProfileController(ProfileService profileService, FileUploadService fileUploadService) {
        this.profileService = profileService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model, RedirectAttributes ra) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());

        model.addAttribute("profile", profile);
        model.addAttribute("educations", profileService.getEducations(profile.getSeekerId()));
        model.addAttribute("experiences", profileService.getExperiences(profile.getSeekerId()));
        model.addAttribute("skills", profileService.getSkills(profile.getSeekerId()));
        model.addAttribute("languages", profileService.getLanguages(profile.getSeekerId()));
        model.addAttribute("certificates", profileService.getCertificates(profile.getSeekerId()));

        model.addAttribute("completionPercentage", profile.getCompletionPercentage());
        model.addAttribute("missingSections", profileService.getMissingSections(profile.getSeekerId()));

        // constants for dropdowns
        model.addAttribute("universities", Constants.VIETNAMESE_UNIVERSITIES);
        model.addAttribute("degreeLevels", Constants.DEGREE_LEVELS);
        model.addAttribute("commonSkills", Constants.COMMON_SKILLS);
        model.addAttribute("recognizedLanguages", Constants.RECOGNIZED_LANGUAGES);

        return "profile-edit";
    }

    // Basic Info Update
    @PostMapping("/profile/basic-info")
    public String updateBasicInfo(
            @RequestParam(required = false) String fullname,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String headline,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(required = false) String about,
            // email đang disabled ở form => thường sẽ không submit; đừng ghi đè null
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            HttpSession session,
            RedirectAttributes ra) {

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            profile.setFullname(fullname);
            profile.setGender(gender);
            profile.setLocation(location);
            profile.setHeadline(headline);
            profile.setExperienceYears(experienceYears != null ? experienceYears : 0);
            profile.setAbout(about);
            // KHÔNG ghi đè email nếu form không gửi (disabled)
            if (email != null && !email.isBlank()) {
                profile.setEmail(email);
            }
            profile.setPhoneNumber(phone);
            profile.setDateOfBirth(dob);

            profileService.updateBasicInfo(profile);
            int percent = profileService.updateCompletionPercentage(profile.getSeekerId());
            ra.addFlashAttribute("success", "Cập nhật thông tin cơ bản thành công (" + percent + "%)");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/jobseeker/profile";
    }

    // Avatar Upload
    @PostMapping("/profile/avatar")
    @ResponseBody // <-- Báo cho Spring biết đây là API trả về JSON
    public ResponseEntity<Map<String, Object>> uploadAvatar( // <-- Thay đổi kiểu trả về
                                                             @RequestParam("avatar") MultipartFile file,
                                                             HttpSession session) { // <-- Bỏ RedirectAttributes ra

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            // Trả về lỗi 401 Unauthorized (Chưa đăng nhập)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // 1. Tải file lên
            String avatarUrl = fileUploadService.uploadAvatar(file);

            // 2. Cập nhật profile trong DB
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            profile.setAvatarUrl(avatarUrl);
            profileService.updateBasicInfo(profile);

            // 3. Cập nhật session (Rất quan trọng, bạn đã làm đúng)
            user.setAvatarUrl(avatarUrl);
            session.setAttribute("user", user);

            // 4. Trả về JSON thành công cho JavaScript
            response.put("success", true);
            response.put("message", "Cập nhật avatar thành công");
            // Gửi URL mới về để JS có thể cập nhật header ngay lập tức
            response.put("newAvatarUrl", avatarUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Lỗi upload avatar: " + e.getMessage());
            // Trả về lỗi 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // ====== Education CRUD ======

    @PostMapping("/profile/education/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addEducation(
            @RequestParam String university,
            @RequestParam String degreeLevel,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate graduationDate,
            @RequestParam(required = false) String description,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            Education education = new Education();
            education.setSeekerId(profile.getSeekerId());
            education.setUniversity(university);
            education.setDegreeLevel(degreeLevel);
            education.setStartDate(startDate);
            education.setGraduationDate(graduationDate);
            education.setDescription(description);

            int id = profileService.addEducation(education);
            profileService.updateCompletionPercentage(profile.getSeekerId());
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/education/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateEducation(
            @RequestParam int educationId,
            @RequestParam String university,
            @RequestParam String degreeLevel,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate graduationDate,
            @RequestParam(required = false) String description,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            Education education = new Education();
            education.setEducationId(educationId);
            education.setSeekerId(profile.getSeekerId());
            education.setUniversity(university);
            education.setDegreeLevel(degreeLevel);
            education.setStartDate(startDate);
            education.setGraduationDate(graduationDate);
            education.setDescription(description);

            profileService.updateEducation(education);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/education/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteEducation(
            @RequestParam int educationId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            profileService.deleteEducation(educationId);
            profileService.updateCompletionPercentage(profile.getSeekerId()); // dùng seekerId, không phải userId
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ====== Experience CRUD ======

    @PostMapping("/profile/experience/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addExperience(
            @RequestParam String jobTitle,
            @RequestParam String companyName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String projectLink,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            Experience experience = new Experience();
            experience.setSeekerId(profile.getSeekerId());
            experience.setJobTitle(jobTitle);
            experience.setCompanyName(companyName);
            experience.setStartDate(startDate);
            experience.setEndDate(endDate);
            experience.setProjectLink(projectLink);

            int id = profileService.addExperience(experience);
            profileService.updateCompletionPercentage(profile.getSeekerId());
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/experience/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateExperience(
            @RequestParam int experienceId,
            @RequestParam String jobTitle,
            @RequestParam String companyName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String projectLink,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            Experience experience = new Experience();
            experience.setExperienceId(experienceId);
            experience.setSeekerId(profile.getSeekerId());
            experience.setJobTitle(jobTitle);
            experience.setCompanyName(companyName);
            experience.setStartDate(startDate);
            experience.setEndDate(endDate);
            experience.setProjectLink(projectLink);

            profileService.updateExperience(experience);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/experience/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteExperience(
            @RequestParam int experienceId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            profileService.deleteExperience(experienceId);
            profileService.updateCompletionPercentage(profile.getSeekerId()); // dùng seekerId
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ====== Skill CRUD ======

    @PostMapping("/profile/skill/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addSkill(
            @RequestParam String skillName,
            @RequestParam int yearsOfExperience,
            @RequestParam(required = false) String description,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            Skill2 skill = new Skill2();
            skill.setSeekerId(profile.getSeekerId());
            skill.setSkillName(skillName);
            skill.setYearsOfExperience(yearsOfExperience);
            skill.setDescription(description);

            int id = profileService.addSkill(skill);
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/skill/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSkill(
            @RequestParam int skillId,
            @RequestParam String skillName,
            @RequestParam int yearsOfExperience,
            @RequestParam(required = false) String description,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            Skill2 skill = new Skill2();
            skill.setSkillId(skillId);
            skill.setSeekerId(profile.getSeekerId());
            skill.setSkillName(skillName);
            skill.setYearsOfExperience(yearsOfExperience);
            skill.setDescription(description);

            profileService.updateSkill(skill);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/skill/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSkill(
            @RequestParam int skillId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            profileService.deleteSkill(skillId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ====== Language CRUD ======

    @PostMapping("/profile/language/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addLanguage(
            @RequestParam String languageName,
            @RequestParam String certificateType,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            Language language = new Language();
            language.setSeekerId(profile.getSeekerId());
            language.setLanguageName(languageName);
            language.setCertificateType(certificateType);

            int id = profileService.addLanguage(language);
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/language/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateLanguage(
            @RequestParam int languageId,
            @RequestParam String languageName,
            @RequestParam String certificateType,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
            Language language = new Language();
            language.setLanguageId(languageId);
            language.setSeekerId(profile.getSeekerId());
            language.setLanguageName(languageName);
            language.setCertificateType(certificateType);

            profileService.updateLanguage(language);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/language/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteLanguage(
            @RequestParam int languageId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            profileService.deleteLanguage(languageId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ====== Certificate CRUD ======

    @PostMapping("/profile/certificate/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addCertificate(
            @RequestParam String issuingOrganization,
            @RequestParam(required = false) MultipartFile certificateImage,
            @RequestParam int yearOfCompletion,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());

            String imageUrl = null;
            if (certificateImage != null && !certificateImage.isEmpty()) {
                imageUrl = fileUploadService.uploadCertificate(certificateImage);
            }

            Certificate certificate = new Certificate();
            certificate.setSeekerId(profile.getSeekerId());
            certificate.setIssuingOrganization(issuingOrganization);
            certificate.setCertificateImageUrl(imageUrl);
            certificate.setYearOfCompletion(yearOfCompletion);

            int id = profileService.addCertificate(certificate);
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/certificate/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCertificate(
            @RequestParam int certificateId,
            @RequestParam String issuingOrganization,
            @RequestParam(required = false) MultipartFile certificateImage,
            @RequestParam int yearOfCompletion,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());

            Certificate certificate = new Certificate();
            certificate.setCertificateId(certificateId);
            certificate.setSeekerId(profile.getSeekerId());
            certificate.setIssuingOrganization(issuingOrganization);
            certificate.setYearOfCompletion(yearOfCompletion);

            if (certificateImage != null && !certificateImage.isEmpty()) {
                String imageUrl = fileUploadService.uploadCertificate(certificateImage);
                certificate.setCertificateImageUrl(imageUrl);
            }

            profileService.updateCertificate(certificate);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/certificate/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCertificate(
            @RequestParam int certificateId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            profileService.deleteCertificate(certificateId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get certificate types for a language
    @GetMapping("/profile/certificate-types")
    @ResponseBody
    public ResponseEntity<String[]> getCertificateTypes(@RequestParam String language) {
        return ResponseEntity.ok(Constants.getCertificateTypes(language));
    }

    // Preview CV
    @GetMapping("/profile/preview")
    public String previewCV(HttpSession session, Model model, RedirectAttributes ra) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());

        model.addAttribute("profile", profile);
        model.addAttribute("educations", profileService.getEducations(profile.getSeekerId()));
        model.addAttribute("experiences", profileService.getExperiences(profile.getSeekerId()));
        model.addAttribute("skills", profileService.getSkills(profile.getSeekerId()));
        model.addAttribute("languages", profileService.getLanguages(profile.getSeekerId()));
        model.addAttribute("certificates", profileService.getCertificates(profile.getSeekerId()));

        return "cv-preview";
    }
}
