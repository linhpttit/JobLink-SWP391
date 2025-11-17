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
            body.append(String.format("Dear %s,\n\n", seekerName));
            body.append(String.format("Great news! We found %d new job opportunity(ies) matching your subscription for %s in %s.\n\n",
                    jobs.size(), skillName, provinceName));
            body.append("=".repeat(60)).append("\n");
            body.append("MATCHING JOB OPPORTUNITIES:\n");
            body.append("=".repeat(60)).append("\n\n");

            int index = 1;
            for (Map<String, Object> job : jobs) {
                body.append(String.format("%d. %s\n", index++, job.get("jobTitle")));
                body.append(String.format("   Company: %s\n", job.get("companyName")));
                body.append(String.format("   Location: %s\n", job.get("location")));
                body.append(String.format("   Salary: $%s - $%s\n",
                        job.get("salaryMin"), job.get("salaryMax")));
                body.append(String.format("   Experience Level: %s\n", job.get("experienceLevel")));
                body.append(String.format("   Job ID: %s\n", job.get("jobId")));
                body.append("\n");
            }

            body.append("=".repeat(60)).append("\n");
            body.append("NEXT STEPS:\n");
            body.append("1. Visit JobLink to view full job details\n");
            body.append("2. Apply to positions that interest you\n");
            body.append("3. Manage your subscriptions anytime\n\n");
            body.append("Best regards,\n");
            body.append("JobLink Team\n\n");
            body.append("---\n");
            body.append("Manage your subscriptions: https://joblink.com/email-subscriptions\n");
            body.append("Unsubscribe: Contact support@joblink.com\n");

            return body.toString();
        }
    }


