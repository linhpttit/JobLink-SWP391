package com.joblink.joblink.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + " (valid for 5 minutes)");
        mailSender.send(message);
    }

    public void sendJobInvitation(String toEmail, String candidateName, String companyName, String jobTitle, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("Lời mời ứng tuyển từ " + companyName);

        // Tạo nội dung email
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Kính gửi ").append(candidateName).append(",\n\n");
        emailBody.append(message).append("\n\n");
        emailBody.append("Vị trí: ").append(jobTitle).append("\n");
        emailBody.append("Công ty: ").append(companyName).append("\n\n");
        emailBody.append("Vui lòng truy cập hệ thống JobLink để xem chi tiết và ứng tuyển.\n\n");
        emailBody.append("Trân trọng,\n");
        emailBody.append(companyName);

        mailMessage.setText(emailBody.toString());
        mailSender.send(mailMessage);
    }
}
