package com.joblink.joblink.service;

import com.joblink.joblink.dao.EmailHistoryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;

@Service
public class EmailSuggestionService {
    private static final Logger logger = LoggerFactory.getLogger(EmailSuggestionService.class);

    private final JavaMailSender mailSender;
    private final EmailHistoryDao emailHistoryDao;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailSuggestionService(JavaMailSender mailSender, EmailHistoryDao emailHistoryDao) {
        this.mailSender = mailSender;
        this.emailHistoryDao = emailHistoryDao;
    }

    /**
     * Send job matching email to seeker
     */
    public void sendJobMatchingEmail(String toEmail, String seekerName,
                                     List<Map<String, Object>> matchedJobs,
                                     String skillName, String provinceName) {
        try {
            String subject = String.format("New Job Opportunities: %s positions in %s",
                    skillName, provinceName);
            String body = buildEmailBody(seekerName, matchedJobs, skillName, provinceName);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send email to: {} - Error: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send subscription confirmation email
     */
    public void sendSubscriptionConfirmationEmail(String toEmail, String seekerName,
                                                  String skillName, String provinceName) {
        try {
            String subject = "JobLink: Subscription Confirmed";
            String body = String.format(
                    "Dear %s,\n\n" +
                            "Your subscription has been confirmed!\n\n" +
                            "Skill: %s\n" +
                            "Location: %s\n\n" +
                            "You will receive email notifications when new job opportunities matching your criteria are posted.\n\n" +
                            "Best regards,\n" +
                            "JobLink Team",
                    seekerName, skillName, provinceName
            );
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Subscription confirmation email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send subscription confirmation email to: {} - Error: {}",
                    toEmail, e.getMessage());
        }
    }

    /**
     * Send subscription cancellation email
     */
    public void sendCancellationEmail(String toEmail, String seekerName,
                                      String skillName, String provinceName) {
        try {
            String subject = "JobLink: Subscription Cancelled";
            String body = String.format(
                    "Dear %s,\n\n" +
                            "Your subscription has been cancelled.\n\n" +
                            "Skill: %s\n" +
                            "Location: %s\n\n" +
                            "You will no longer receive job notifications for this subscription.\n" +
                            "To resubscribe, visit your subscription management page.\n\n" +
                            "Best regards,\n" +
                            "JobLink Team",
                    seekerName, skillName, provinceName
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Cancellation email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send cancellation email to: {} - Error: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Build HTML-style email body with job listings
     */
    private String buildEmailBody(String seekerName, List<Map<String, Object>> jobs,
                                  String skillName, String provinceName) {
        StringBuilder body = new StringBuilder();
        body.append(String.format("Xin chào %s,\n\n", seekerName));
        body.append(String.format("Tin tốt! Chúng tôi đã tìm thấy %d cơ hội việc làm mới phù hợp với đăng ký của bạn cho %s tại %s.\n\n",
                jobs.size(), skillName, provinceName));
        body.append("=".repeat(60)).append("\n");
        body.append("CƠ HỘI VIỆC LÀM PHÙ HỢP:\n");
        body.append("=".repeat(60)).append("\n\n");

        int index = 1;
        for (Map<String, Object> job : jobs) {
            body.append(String.format("%d. %s\n", index++, job.get("jobTitle") != null ? job.get("jobTitle") : job.get("title")));
            body.append(String.format("   Công ty: %s\n", job.get("companyName") != null ? job.get("companyName") : "N/A"));
            body.append(String.format("   Địa điểm: %s\n", job.get("location") != null ? job.get("location") : provinceName));
            
            Object salaryMin = job.get("salaryMin");
            Object salaryMax = job.get("salaryMax");
            if (salaryMin != null || salaryMax != null) {
                String salaryStr = "";
                if (salaryMin != null && salaryMax != null) {
                    salaryStr = String.format("%s - %s VNĐ", formatSalary(salaryMin), formatSalary(salaryMax));
                } else if (salaryMin != null) {
                    salaryStr = "Từ " + formatSalary(salaryMin) + " VNĐ";
                } else if (salaryMax != null) {
                    salaryStr = "Đến " + formatSalary(salaryMax) + " VNĐ";
                }
                body.append(String.format("   Mức lương: %s\n", salaryStr));
            }
            
            body.append(String.format("   Kinh nghiệm: %s\n", job.get("experienceLevel") != null ? job.get("experienceLevel") : job.get("year_experience")));
            body.append(String.format("   Loại công việc: %s\n", job.get("work_type") != null ? job.get("work_type") : "N/A"));
            body.append(String.format("   Xem chi tiết: http://localhost:8080/job/%s\n", job.get("job_id") != null ? job.get("job_id") : job.get("jobId")));
            body.append("\n");
        }

        body.append("=".repeat(60)).append("\n");
        body.append("BƯỚC TIẾP THEO:\n");
        body.append("1. Truy cập JobLink để xem chi tiết công việc\n");
        body.append("2. Ứng tuyển vào các vị trí bạn quan tâm\n");
        body.append("3. Quản lý đăng ký của bạn bất cứ lúc nào\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ JobLink\n\n");
        body.append("---\n");
        body.append("Quản lý đăng ký: http://localhost:8080/jobseeker/email-subscriptions\n");
        body.append("Hủy đăng ký: Vào trang quản lý đăng ký và nhấn nút 'Hủy'\n");

        return body.toString();
    }

    /**
     * Format salary number
     */
    private String formatSalary(Object salary) {
        if (salary == null) return "0";
        try {
            double amount = Double.parseDouble(salary.toString());
            if (amount >= 1000000) {
                return String.format("%.0f triệu", amount / 1000000);
            }
            return String.format("%.0f", amount);
        } catch (Exception e) {
            return salary.toString();
        }
    }
}
