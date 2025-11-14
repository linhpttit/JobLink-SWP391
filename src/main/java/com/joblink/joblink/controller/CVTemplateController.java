package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.model.*;
import com.joblink.joblink.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.joblink.joblink.dto.UserSessionDTO;

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
        UserSessionDTO userSession = (UserSessionDTO) session.getAttribute("user");  // ✅ FIX
        if (userSession == null) return "redirect:/auth/login";

        if (!premiumService.hasFeature(userSession.getUserId(), "cv_templates")) {  // ✅ FIX
            return "redirect:/jobseeker/premium";
        }

        List<CVTemplate> templates = cvTemplateService.getAllActiveTemplates();
        model.addAttribute("templates", templates);
        model.addAttribute("user", userSession);  // ✅ FIX
        model.addAttribute("hasCVAccess", true);
        return "cv-templates";
    }

    @GetMapping("/{templateId}")
    public String viewTemplate(@PathVariable int templateId,
                               HttpSession session,
                               Model model) {
        UserSessionDTO userSession = (UserSessionDTO) session.getAttribute("user");  // ✅ FIX
        if (userSession == null) return "redirect:/auth/login";

        if (!premiumService.hasFeature(userSession.getUserId(), "cv_templates")) {  // ✅ FIX
            return "redirect:/jobseeker/premium";
        }

        CVTemplate template = cvTemplateService.getTemplateById(templateId);
        if (template == null) {
            model.addAttribute("error", "Template not found");
            return "redirect:/jobseeker/cv-templates";
        }

        JobSeekerProfile2 profile = jobSeekerService.getProfileByUserId(userSession.getUserId());  // ✅ FIX
        if (profile == null) {
            model.addAttribute("error", "Please create your JobSeeker profile first.");
            return "redirect:/jobseeker/profile/edit";
        }

        model.addAttribute("template", template);
        model.addAttribute("profile", profile);
        model.addAttribute("user", userSession);  // ✅ FIX
        return "cv-template-view";
    }

    /**
     * SINGLE export endpoint (JSON -> returns download URL)
     */
    @PostMapping("/{templateId}/export")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportTemplate(@PathVariable int templateId,
                                                              HttpSession session) {
        UserSessionDTO userSession = (UserSessionDTO) session.getAttribute("user");  // ✅ FIX
        Map<String, Object> res = new HashMap<>();
        if (userSession == null) return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));

        if (!premiumService.hasFeature(userSession.getUserId(), "cv_templates")) {  // ✅ FIX
            return ResponseEntity.status(403).body(Map.of("success", false, "error", "Premium access required"));
        }

        JobSeekerProfile2 profile = jobSeekerService.getProfileByUserId(userSession.getUserId());  // ✅ FIX
        if (profile == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Profile not found"));
        }

        CVTemplate template = cvTemplateService.getTemplateById(templateId);
        if (template == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "error", "Template not found"));
        }

        String safeName = (profile.getFullname() == null ? "CV" : profile.getFullname()).replaceAll("\\s+", "_");
        String fileName = "CV_" + safeName + "_" + template.getTemplateCode() + ".pdf";
        cvTemplateService.saveExportRecord(profile.getSeekerId(), templateId, fileName);

        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        res.put("success", true);
        res.put("downloadUrl", "/jobseeker/cv-templates/download/" + templateId + "?fn=" + encoded);
        res.put("message", "CV exported successfully!");
        return ResponseEntity.ok(res);
    }

    /**
     * Download (re-generate on the fly)
     */
    @GetMapping("/download/{templateId}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable int templateId,
                                                   @RequestParam(name = "fn", required = false) String fn,
                                                   HttpSession session) {
        UserSessionDTO userSession = (UserSessionDTO) session.getAttribute("user");  // ✅ FIX
        if (userSession == null) return ResponseEntity.status(401).build();
        if (!premiumService.hasFeature(userSession.getUserId(), "cv_templates")) return ResponseEntity.status(403).build();  // ✅ FIX

        JobSeekerProfile2 profile = jobSeekerService.getProfileByUserId(userSession.getUserId());  // ✅ FIX
        if (profile == null) return ResponseEntity.badRequest().build();

        CVTemplate template = cvTemplateService.getTemplateById(templateId);
        if (template == null) return ResponseEntity.status(404).build();

        byte[] pdf = cvTemplateService.generatePDF(template, profile);

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
