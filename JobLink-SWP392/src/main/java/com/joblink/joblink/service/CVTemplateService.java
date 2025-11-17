
package com.joblink.joblink.service;

import com.joblink.joblink.dao.CVTemplateDao;
import com.joblink.joblink.dao.CVExportDao;
import com.joblink.joblink.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CVTemplateService {
    private final CVTemplateDao cvTemplateDao;
    private final CVExportDao cvExportDao;
    private final EducationService educationService;
    private final ExperienceService experienceService;
    private final SkillService skillService;

    public CVTemplateService(CVTemplateDao cvTemplateDao,
                             CVExportDao cvExportDao,
                             EducationService educationService,
                             ExperienceService experienceService,
                             SkillService skillService) {
        this.cvTemplateDao = cvTemplateDao;
        this.cvExportDao = cvExportDao;
        this.educationService = educationService;
        this.experienceService = experienceService;
        this.skillService = skillService;
    }

    public List<CVTemplate> getAllActiveTemplates() {
        return cvTemplateDao.findAllActive();
    }

    public CVTemplate getTemplateById(int templateId) {
        return cvTemplateDao.findById(templateId);
    }

    public CVTemplate getTemplateByCode(String code) {
        return cvTemplateDao.findByCode(code);
    }

    public List<Experience> getExperiences(int seekerId) {
        return experienceService.getExperiencesBySeekerId(seekerId);
    }

    public List<Education> getEducations(int seekerId) {
        return educationService.getEducationsBySeekerId(seekerId);
    }

    public List<Skill2> getSkills(int seekerId) {
        return skillService.getSkillsBySeekerId(seekerId);
    }

    public String renderTemplate(CVTemplate template, JobSeekerProfile2 profile) {
        return renderTemplate(template, profile, null);
    }

    private String renderExperiences(String html, List<Experience> experiences) {
        // Find the {{#each experiences}} block
        Pattern pattern = Pattern.compile("\\{\\{#each experiences\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find() && experiences != null && !experiences.isEmpty()) {
            String template = matcher.group(1);
            StringBuilder rendered = new StringBuilder();

            for (Experience exp : experiences) {
                String expHtml = template;
                expHtml = expHtml.replace("{{job_title}}", exp.getJobTitle() != null ? exp.getJobTitle() : "");
                expHtml = expHtml.replace("{{company_name}}", exp.getCompanyName() != null ? exp.getCompanyName() : "");
                
                // Format dates
                String startDate = "";
                String endDate = "Present";
                if (exp.getStartDate() != null) {
                    startDate = exp.getStartDate().toString();
                }
                if (exp.getEndDate() != null) {
                    endDate = exp.getEndDate().toString();
                }
                
                expHtml = expHtml.replace("{{start_date}}", startDate);
                expHtml = expHtml.replace("{{end_date}}", endDate);
                expHtml = expHtml.replace("{{project_link}}", exp.getProjectLink() != null ? exp.getProjectLink() : "");
                rendered.append(expHtml);
            }

            html = matcher.replaceFirst(rendered.toString());
        } else if (matcher.find()) {
            // Remove the template block if no experiences
            html = matcher.replaceFirst("");
        }

        return html;
    }

    private String renderEducations(String html, List<Education> educations) {
        Pattern pattern = Pattern.compile("\\{\\{#each educations\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find() && educations != null && !educations.isEmpty()) {
            String template = matcher.group(1);
            StringBuilder rendered = new StringBuilder();

            for (Education edu : educations) {
                String eduHtml = template;
                eduHtml = eduHtml.replace("{{degree_level}}", edu.getDegreeLevel() != null ? edu.getDegreeLevel() : "");
                eduHtml = eduHtml.replace("{{university}}", edu.getUniversity() != null ? edu.getUniversity() : "");
                
                // Format dates
                String startDate = "";
                String graduationDate = "";
                if (edu.getStartDate() != null) {
                    startDate = edu.getStartDate().toString();
                }
                if (edu.getGraduationDate() != null) {
                    graduationDate = edu.getGraduationDate().toString();
                }
                
                eduHtml = eduHtml.replace("{{start_date}}", startDate);
                eduHtml = eduHtml.replace("{{graduation_date}}", graduationDate);
                rendered.append(eduHtml);
            }

            html = matcher.replaceFirst(rendered.toString());
        } else if (matcher.find()) {
            // Remove the template block if no educations
            html = matcher.replaceFirst("");
        }

        return html;
    }

    private String renderSkills(String html, List<Skill2> skills) {
        Pattern pattern = Pattern.compile("\\{\\{#each skills\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find() && skills != null && !skills.isEmpty()) {
            String template = matcher.group(1);
            StringBuilder rendered = new StringBuilder();

            for (Skill2 skill : skills) {
                String skillHtml = template;
                skillHtml = skillHtml.replace("{{skill_name}}", skill.getSkillName() != null ? skill.getSkillName() : "");
                skillHtml = skillHtml.replace("{{years_of_experience}}", skill.getYearsOfExperience() != null ? skill.getYearsOfExperience().toString() : "0");
                // Calculate skill percentage (max 5 years = 100%)
                int percentage = skill.getYearsOfExperience() != null ? Math.min(skill.getYearsOfExperience() * 20, 100) : 0;
                skillHtml = skillHtml.replace("{{skill_percentage}}", String.valueOf(percentage));
                rendered.append(skillHtml);
            }

            html = matcher.replaceFirst(rendered.toString());
        } else if (matcher.find()) {
            // Remove the template block if no skills
            html = matcher.replaceFirst("");
        }

        return html;
    }

    public byte[] generatePDF(CVTemplate template, JobSeekerProfile2 profile) {
        return generatePDF(template, profile, null);
    }

    public byte[] generatePDF(CVTemplate template, JobSeekerProfile2 profile, Map<String, Object> customizations) {
        try {
            // Render template with customizations
            String renderedHTML = renderTemplate(template, profile, customizations);
            String css = template.getCssContent() != null ? template.getCssContent() : "";
            
            // Apply customizations to CSS
            if (customizations != null) {
                String primaryColor = (String) customizations.getOrDefault("primaryColor", "#3b82f6");
                css = css.replaceAll("#3b82f6", primaryColor);
                
                String fontSize = (String) customizations.getOrDefault("fontSize", "medium");
                String fontSizeValue = fontSize.equals("small") ? "14px" : fontSize.equals("large") ? "18px" : "16px";
                css = css.replaceAll("font-size:\\s*16px", "font-size: " + fontSizeValue);
            }
            
            String fullHTML = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" + css + "</style></head><body>" + renderedHTML + "</body></html>";

            // Use OpenHTMLToPDF to convert HTML to PDF
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.withHtmlContent(fullHTML, null);
            builder.useDefaultPageSize(210, 297, com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PageSizeUnits.MM);
            builder.toStream(baos);
            builder.run();
            
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: return HTML as bytes if PDF generation fails
            String renderedHTML = renderTemplate(template, profile, customizations);
            String fullHTML = "<!DOCTYPE html><html><head><style>" + template.getCssContent() + "</style></head><body>" + renderedHTML + "</body></html>";
            return fullHTML.getBytes();
        }
    }

    public String renderTemplate(CVTemplate template, JobSeekerProfile2 profile, Map<String, Object> customizations) {
        String html = template.getHtmlContent();

        // Use custom profile data if provided
        String fullname = profile.getFullname();
        String email = profile.getEmail();
        String phone = profile.getPhoneNumber();
        String location = profile.getLocation();
        String headline = profile.getHeadline();
        String about = profile.getAbout();

        if (customizations != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> profileData = (Map<String, Object>) customizations.get("profileData");
            if (profileData != null) {
                fullname = (String) profileData.getOrDefault("fullname", fullname);
                email = (String) profileData.getOrDefault("email", email);
                phone = (String) profileData.getOrDefault("phone", phone);
                location = (String) profileData.getOrDefault("location", location);
                headline = (String) profileData.getOrDefault("headline", headline);
                about = (String) profileData.getOrDefault("about", about);
            }
        }

        // Replace basic profile fields
        html = html.replace("{{fullname}}", fullname != null ? fullname : "");
        html = html.replace("{{headline}}", headline != null ? headline : "");
        html = html.replace("{{email}}", email != null ? email : "");
        html = html.replace("{{phone}}", phone != null ? phone : "");
        html = html.replace("{{location}}", location != null ? location : "");
        html = html.replace("{{about}}", about != null ? about : "");

        // Replace primary color if customizations provided
        if (customizations != null) {
            String primaryColor = (String) customizations.getOrDefault("primaryColor", "#3b82f6");
            html = html.replace("{{primaryColor}}", primaryColor);
        } else {
            html = html.replace("{{primaryColor}}", "#3b82f6");
        }

        // Generate initials for avatar
        String initials = "";
        if (fullname != null && !fullname.isEmpty()) {
            String[] names = fullname.split(" ");
            if (names.length > 0) {
                initials = names[0].substring(0, 1).toUpperCase();
                if (names.length > 1) {
                    initials += names[names.length - 1].substring(0, 1).toUpperCase();
                }
            }
        }
        html = html.replace("{{initials}}", initials);

        // Get and render experiences (only if showExperience is true)
        boolean showExperience = customizations == null || 
            (Boolean) customizations.getOrDefault("showExperience", true);
        if (showExperience) {
            List<Experience> experiences = experienceService.getExperiencesBySeekerId(profile.getSeekerId());
            html = renderExperiences(html, experiences);
        } else {
            html = html.replaceAll("\\{\\{#each experiences\\}\\}.*?\\{\\{/each\\}\\}", "");
        }

        // Get and render educations (only if showEducation is true)
        boolean showEducation = customizations == null || 
            (Boolean) customizations.getOrDefault("showEducation", true);
        if (showEducation) {
            List<Education> educations = educationService.getEducationsBySeekerId(profile.getSeekerId());
            html = renderEducations(html, educations);
        } else {
            html = html.replaceAll("\\{\\{#each educations\\}\\}.*?\\{\\{/each\\}\\}", "");
        }

        // Get and render skills (only if showSkills is true)
        boolean showSkills = customizations == null || 
            (Boolean) customizations.getOrDefault("showSkills", true);
        if (showSkills) {
            List<Skill2> skills = skillService.getSkillsBySeekerId(profile.getSeekerId());
            html = renderSkills(html, skills);
        } else {
            html = html.replaceAll("\\{\\{#each skills\\}\\}.*?\\{\\{/each\\}\\}", "");
        }

        return html;
    }

    @Transactional
    public void saveExportRecord(int seekerId, int templateId, String fileName) {
        CVExport export = new CVExport();
        export.setSeekerId(seekerId);
        export.setTemplateId(templateId);
        export.setFileName(fileName);
        export.setFilePath("/exports/" + fileName);
        export.setFileSizeKb(0); // Calculate actual size
        export.setExportedAt(LocalDateTime.now());

        cvExportDao.create(export);
    }

    @Transactional
    public void saveCustomizations(int seekerId, int templateId, Map<String, Object> customizations, Map<String, Object> profileData) {
        // Save customizations to database (you may need to create a table for this)
        // For now, we'll just store it in CVExport or create a new table
        // This is a placeholder - implement based on your database schema
        try {
            // You can store customizations as JSON in a new column or table
            // For now, we'll just log it
            System.out.println("Saving customizations for seekerId: " + seekerId + ", templateId: " + templateId);
            if (customizations != null) {
                System.out.println("Customizations: " + customizations);
            }
            if (profileData != null) {
                System.out.println("Profile Data: " + profileData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save customizations", e);
        }
    }
}
