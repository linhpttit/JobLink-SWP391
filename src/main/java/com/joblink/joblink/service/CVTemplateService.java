
package com.joblink.joblink.service;

import com.joblink.joblink.dao.CVTemplateDao;
import com.joblink.joblink.dao.CVExportDao;
import com.joblink.joblink.model.*;
        import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    public String renderTemplate(CVTemplate template, JobSeekerProfile profile) {
        String html = template.getHtmlContent();

        // Replace basic profile fields
        html = html.replace("{{fullname}}", profile.getFullname() != null ? profile.getFullname() : "");
        html = html.replace("{{headline}}", profile.getHeadline() != null ? profile.getHeadline() : "");
        html = html.replace("{{email}}", profile.getEmail() != null ? profile.getEmail() : "");
        html = html.replace("{{phone}}", profile.getPhoneNumber()!= null ? profile.getPhoneNumber() : "");
        html = html.replace("{{location}}", profile.getLocation() != null ? profile.getLocation() : "");
        html = html.replace("{{about}}", profile.getAbout() != null ? profile.getAbout() : "");
//        html = html.replace("{{github_url}}", profile.getGithubUrl() != null ? profile.getGithubUrl() : "");
//        html = html.replace("{{linkedin_url}}", profile.getLinkedinUrl() != null ? profile.getLinkedinUrl() : "");
//        html = html.replace("{{website}}", profile.getWebsite() != null ? profile.getWebsite() : "");

        // Generate initials for avatar
        String initials = "";
        if (profile.getFullname() != null && !profile.getFullname().isEmpty()) {
            String[] names = profile.getFullname().split(" ");
            if (names.length > 0) {
                initials = names[0].substring(0, 1).toUpperCase();
                if (names.length > 1) {
                    initials += names[names.length - 1].substring(0, 1).toUpperCase();
                }
            }
        }
        html = html.replace("{{initials}}", initials);

        // Get and render experiences
        List<Experience> experiences = experienceService.getExperiencesBySeekerId(profile.getSeekerId());
        html = renderExperiences(html, experiences);

        // Get and render educations
        List<Education> educations = educationService.getEducationsBySeekerId(profile.getSeekerId());
        html = renderEducations(html, educations);

        // Get and render skills
        List<Skill> skills = skillService.getSkillsBySeekerId(profile.getSeekerId());
        html = renderSkills(html, skills);

        return html;
    }

    private String renderExperiences(String html, List<Experience> experiences) {
        // Find the {{#each experiences}} block
        Pattern pattern = Pattern.compile("\\{\\{#each experiences\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {
            String template = matcher.group(1);
            StringBuilder rendered = new StringBuilder();

            for (Experience exp : experiences) {
                String expHtml = template;
                expHtml = expHtml.replace("{{job_title}}", exp.getJobTitle() != null ? exp.getJobTitle() : "");
                expHtml = expHtml.replace("{{company_name}}", exp.getCompanyName() != null ? exp.getCompanyName() : "");
                expHtml = expHtml.replace("{{start_date}}", exp.getStartDate() != null ? exp.getStartDate().toString() : "");
                expHtml = expHtml.replace("{{end_date}}", exp.getEndDate() != null ? exp.getEndDate().toString() : "Present");
                expHtml = expHtml.replace("{{project_link}}", exp.getProjectLink() != null ? exp.getProjectLink() : "");
                rendered.append(expHtml);
            }

            html = matcher.replaceFirst(rendered.toString());
        }

        return html;
    }

    private String renderEducations(String html, List<Education> educations) {
        Pattern pattern = Pattern.compile("\\{\\{#each educations\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {
            String template = matcher.group(1);
            StringBuilder rendered = new StringBuilder();

            for (Education edu : educations) {
                String eduHtml = template;
                eduHtml = eduHtml.replace("{{degree_level}}", edu.getDegreeLevel() != null ? edu.getDegreeLevel() : "");
                eduHtml = eduHtml.replace("{{university}}", edu.getUniversity() != null ? edu.getUniversity() : "");
                eduHtml = eduHtml.replace("{{start_date}}", edu.getStartDate() != null ? edu.getStartDate().toString() : "");
                eduHtml = eduHtml.replace("{{graduation_date}}", edu.getGraduationDate() != null ? edu.getGraduationDate().toString() : "");
                rendered.append(eduHtml);
            }

            html = matcher.replaceFirst(rendered.toString());
        }

        return html;
    }

    private String renderSkills(String html, List<Skill> skills) {
        Pattern pattern = Pattern.compile("\\{\\{#each skills\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {
            String template = matcher.group(1);
            StringBuilder rendered = new StringBuilder();

            for (Skill skill : skills) {
                String skillHtml = template;
                skillHtml = skillHtml.replace("{{skill_name}}", skill.getSkillName() != null ? skill.getSkillName() : "");
                skillHtml = skillHtml.replace("{{years_of_experience}}", skill.getYearsOfExperience() != null ? skill.getYearsOfExperience().toString() : "0");
                // Calculate skill percentage (max 5 years = 100%)
                int percentage = skill.getYearsOfExperience() != null ? Math.min(skill.getYearsOfExperience() * 20, 100) : 0;
                skillHtml = skillHtml.replace("{{skill_percentage}}", String.valueOf(percentage));
                rendered.append(skillHtml);
            }

            html = matcher.replaceFirst(rendered.toString());
        }

        return html;
    }

    public byte[] generatePDF(CVTemplate template, JobSeekerProfile profile) {
        // For demo purposes, return a simple PDF placeholder
        // In production, use a library like iText or Flying Saucer to convert HTML to PDF
        String renderedHTML = renderTemplate(template, profile);
        String fullHTML = "<!DOCTYPE html><html><head><style>" + template.getCssContent() + "</style></head><body>" + renderedHTML + "</body></html>";

        // This is a placeholder - in production, use proper PDF generation
        return fullHTML.getBytes();
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
}
