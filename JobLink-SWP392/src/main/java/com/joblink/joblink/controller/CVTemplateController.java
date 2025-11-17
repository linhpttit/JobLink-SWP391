package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.model.*;
import com.joblink.joblink.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobseeker/cv-templates")
public class CVTemplateController {

    private final CVTemplateService cvTemplateService;
    private final PremiumService premiumService;
    private final JobSeekerService jobSeekerService;

    public CVTemplateController(CVTemplateService cvTemplateService,
                                PremiumService premiumService,
                                JobSeekerService jobSeekerService) {
        this.cvTemplateService = cvTemplateService;
        this.premiumService = premiumService;
        this.jobSeekerService = jobSeekerService;
    }

    @GetMapping
    public String showTemplates(HttpSession session, Model model) {
        UserSessionDTO userSession = (UserSessionDTO) session.getAttribute("user");
        if (userSession == null) return "redirect:/auth/login";

        if (!premiumService.hasFeature(userSession.getUserId(), "cv_templates")) {
            model.addAttribute("error", "Bạn cần nâng cấp lên gói Premium để sử dụng tính năng CV Templates");
            return "redirect:/payment/packages";
        }

        // Create list of 5 FlowCV templates with images and links
        List<Map<String, Object>> flowcvTemplates = new java.util.ArrayList<>();
        
        // Template 1: Atlantic Blue
        Map<String, Object> template1 = new HashMap<>();
        template1.put("id", 1);
        template1.put("name", "Atlantic Blue");
        template1.put("description", "Multi-column sidebar left design");
        template1.put("imageUrl", "/images/cv-templates/atlantic-blue.jpg"); // Place your image at: src/main/resources/static/images/cv-templates/atlantic-blue.jpg
        template1.put("externalLink", "https://flowcv.com/resume-template/atlantic-blue-multi-column-sidebar-left");
        flowcvTemplates.add(template1);
        
        // Template 2: Executive
        Map<String, Object> template2 = new HashMap<>();
        template2.put("id", 2);
        template2.put("name", "Executive");
        template2.put("description", "Serif black and white professional design");
        template2.put("imageUrl", "/images/cv-templates/executive.jpg"); // Place your image at: src/main/resources/static/images/cv-templates/executive.jpg
        template2.put("externalLink", "https://flowcv.com/resume-template/executive-serif-black-white");
        flowcvTemplates.add(template2);
        
        // Template 3: Classic
        Map<String, Object> template3 = new HashMap<>();
        template3.put("id", 3);
        template3.put("name", "Classic");
        template3.put("description", "One-column design for professionals");
        template3.put("imageUrl", "/images/cv-templates/classic.jpg"); // Place your image at: src/main/resources/static/images/cv-templates/classic.jpg
        template3.put("externalLink", "https://flowcv.com/resume-template/classic-one-column-design-professionals");
        flowcvTemplates.add(template3);
        
        // Template 4: Leaves
        Map<String, Object> template4 = new HashMap<>();
        template4.put("id", 4);
        template4.put("name", "Leaves");
        template4.put("description", "Dark leaves multi-column border left");
        template4.put("imageUrl", "/images/cv-templates/leaves.jpg"); // Place your image at: src/main/resources/static/images/cv-templates/leaves.jpg
        template4.put("externalLink", "https://flowcv.com/resume-template/dark-leaves-multi-column-border-left");
        flowcvTemplates.add(template4);
        
        // Template 5: Harvard
        Map<String, Object> template5 = new HashMap<>();
        template5.put("id", 5);
        template5.put("name", "Harvard");
        template5.put("description", "Classic sans-serif style");
        template5.put("imageUrl", "/images/cv-templates/harvard.jpg"); // Place your image at: src/main/resources/static/images/cv-templates/harvard.jpg
        template5.put("externalLink", "https://flowcv.com/resume-template/harvard-classic-sans-serif-style");
        flowcvTemplates.add(template5);
        
        // Debug log
        System.out.println("User ID: " + userSession.getUserId());
        System.out.println("Has CV Templates access: " + premiumService.hasFeature(userSession.getUserId(), "cv_templates"));
        System.out.println("Number of FlowCV templates: " + flowcvTemplates.size());
        
        model.addAttribute("flowcvTemplates", flowcvTemplates);
        model.addAttribute("user", userSession);
        model.addAttribute("hasCVAccess", true);
        return "cv-templates";
    }

    @GetMapping("/{templateId}")
    public String viewTemplate(@PathVariable int templateId,
                               @RequestParam(required = false) String mode,
                               HttpSession session,
                               Model model) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";

        if (!premiumService.hasFeature(user.getUserId(), "cv_templates")) {
            return "redirect:/payment/packages";
        }

        CVTemplate template = cvTemplateService.getTemplateById(templateId);
        if (template == null) {
            model.addAttribute("error", "Template not found");
            return "redirect:/jobseeker/cv-templates";
        }

        JobSeekerProfile2 profile = jobSeekerService.getProfileByUserId(user.getUserId());
        if (profile == null) {
            model.addAttribute("error", "Please create your JobSeeker profile first.");
            return "redirect:/jobseeker/profile/edit";
        }

        // Get related data
        List<Experience> experiences = cvTemplateService.getExperiences(profile.getSeekerId());
        List<Education> educations = cvTemplateService.getEducations(profile.getSeekerId());
        List<Skill2> skills = cvTemplateService.getSkills(profile.getSeekerId());

        // Debug logs
        System.out.println("Template ID: " + templateId);
        System.out.println("Template Name: " + template.getTemplateName());
        System.out.println("Template HTML Content Length: " + (template.getHtmlContent() != null ? template.getHtmlContent().length() : 0));
        System.out.println("Profile Fullname: " + profile.getFullname());
        System.out.println("Experiences count: " + (experiences != null ? experiences.size() : 0));
        System.out.println("Educations count: " + (educations != null ? educations.size() : 0));
        System.out.println("Skills count: " + (skills != null ? skills.size() : 0));

        model.addAttribute("template", template);
        model.addAttribute("profile", profile);
        model.addAttribute("user", user);
        model.addAttribute("experiences", experiences != null ? experiences : java.util.Collections.emptyList());
        model.addAttribute("educations", educations != null ? educations : java.util.Collections.emptyList());
        model.addAttribute("skills", skills != null ? skills : java.util.Collections.emptyList());

        // Return editor if mode=edit, otherwise return view
        if ("edit".equals(mode)) {
            return "cv-template-editor";
        }
        return "cv-template-view";
    }

    /**
     * Save CV customizations
     */
    @PostMapping("/{templateId}/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveCV(@PathVariable int templateId,
                                                       @RequestBody Map<String, Object> payload,
                                                       HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        if (!premiumService.hasFeature(user.getUserId(), "cv_templates")) {
            return ResponseEntity.status(403).body(Map.of("success", false, "error", "Premium access required"));
        }

        try {
            JobSeekerProfile2 profile = jobSeekerService.getProfileByUserId(user.getUserId());
            if (profile == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Profile not found"));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> customizations = (Map<String, Object>) payload.get("customizations");
            @SuppressWarnings("unchecked")
            Map<String, Object> profileData = (Map<String, Object>) payload.get("profileData");

            cvTemplateService.saveCustomizations(profile.getSeekerId(), templateId, customizations, profileData);

            return ResponseEntity.ok(Map.of("success", true, "message", "CV saved successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * SINGLE export endpoint (JSON -> returns download URL)
     */
    @PostMapping("/{templateId}/export")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportTemplate(@PathVariable int templateId,
                                                              @RequestBody(required = false) Map<String, Object> payload,
                                                              HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        Map<String, Object> res = new HashMap<>();
        if (user == null) return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));

        if (!premiumService.hasFeature(user.getUserId(), "cv_templates")) {
            return ResponseEntity.status(403).body(Map.of("success", false, "error", "Premium access required"));
        }

        JobSeekerProfile2 profile = jobSeekerService.getProfileByUserId(user.getUserId());
        if (profile == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Profile not found"));
        }

        CVTemplate template = cvTemplateService.getTemplateById(templateId);
        if (template == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "error", "Template not found"));
        }

        // Get customizations from payload if provided
        Map<String, Object> customizations = null;
        if (payload != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cust = (Map<String, Object>) payload.get("customizations");
            customizations = cust;
        }

        // Lưu lịch sử export (nếu bạn muốn lưu) – tên file an toàn
        String safeName = (profile.getFullname() == null ? "CV" : profile.getFullname()).replaceAll("\\s+", "_");
        String fileName = "CV_" + safeName + "_" + template.getTemplateCode() + ".pdf";
        cvTemplateService.saveExportRecord(profile.getSeekerId(), templateId, fileName);

        // Encode customizations as JSON for URL parameter
        String customizationsJson = "";
        if (customizations != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                customizationsJson = URLEncoder.encode(mapper.writeValueAsString(customizations), StandardCharsets.UTF_8);
            } catch (Exception e) {
                // Ignore encoding errors
            }
        }

        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String downloadUrl = "/jobseeker/cv-templates/download/" + templateId + "?fn=" + encoded;
        if (!customizationsJson.isEmpty()) {
            downloadUrl += "&customizations=" + customizationsJson;
        }
        
        res.put("success", true);
        res.put("downloadUrl", downloadUrl);
        res.put("message", "CV exported successfully!");
        return ResponseEntity.ok(res);
    }

    /**
     * Download (re-generate on the fly)
     */
    @GetMapping("/download/{templateId}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable int templateId,
                                                   @RequestParam(name = "fn", required = false) String fn,
                                                   @RequestParam(name = "customizations", required = false) String customizationsJson,
                                                   HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();
        if (!premiumService.hasFeature(user.getUserId(), "cv_templates")) return ResponseEntity.status(403).build();

        JobSeekerProfile2 profile = jobSeekerService.getProfileByUserId(user.getUserId());
        if (profile == null) return ResponseEntity.badRequest().build();

        CVTemplate template = cvTemplateService.getTemplateById(templateId);
        if (template == null) return ResponseEntity.status(404).build();

        // Parse customizations if provided
        Map<String, Object> customizations = null;
        if (customizationsJson != null && !customizationsJson.isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = mapper.readValue(customizationsJson, Map.class);
                customizations = parsed;
            } catch (Exception e) {
                // Ignore parsing errors, use default
            }
        }

        byte[] pdf = cvTemplateService.generatePDF(template, profile, customizations);
        String fallback = "CV_" + (profile.getFullname() == null ? "User" : profile.getFullname().replaceAll("\\s+", "_"))
                + "_" + template.getTemplateCode() + ".pdf";
        String fileName = (fn == null || fn.isBlank()) ? fallback : fn;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}