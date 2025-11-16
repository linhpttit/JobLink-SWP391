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
    public void sendEmployerStatusNotification(String toEmail, String companyName, boolean isAccepted) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);

            if (isAccepted) {
                message.setSubject("Thông báo: Tài khoản nhà tuyển dụng đã được chấp nhận");
                message.setText(buildAcceptedEmailBody(companyName));
            } else {
                message.setSubject("Thông báo: Tài khoản nhà tuyển dụng bị từ chối");
                message.setText(buildRejectedEmailBody(companyName));
            }

            mailSender.send(message);
            System.out.println("✅ Đã gửi email thông báo trạng thái cho employer: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email thông báo cho employer: " + e.getMessage());
            e.printStackTrace();
            // Không throw exception để không ảnh hưởng đến việc cập nhật trạng thái
        }
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
    private String buildAcceptedEmailBody(String companyName) {
        return String.format(
                "Kính gửi Quý công ty %s,\n\n" +
                        "Chúng tôi rất vui mừng thông báo rằng tài khoản nhà tuyển dụng của Quý công ty đã được xét duyệt và chấp nhận thành công.\n\n" +
                        "Quý công ty có thể:\n" +
                        "- Đăng nhập vào hệ thống JobLink\n" +
                        "- Đăng tin tuyển dụng\n" +
                        "- Quản lý các ứng viên\n" +
                        "- Sử dụng đầy đủ các tính năng dành cho nhà tuyển dụng\n\n" +
                        "Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi qua email hỗ trợ.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ JobLink",
                companyName
        );
    }

    private String buildRejectedEmailBody(String companyName) {
        return String.format(
                "Kính gửi Quý công ty %s,\n\n" +
                        "Chúng tôi rất tiếc phải thông báo rằng tài khoản nhà tuyển dụng của Quý công ty đã bị từ chối sau quá trình xét duyệt.\n\n" +
                        "Nếu Quý công ty có bất kỳ thắc mắc nào về quyết định này, vui lòng liên hệ với chúng tôi qua email hỗ trợ để được giải đáp.\n\n" +
                        "Chúng tôi rất mong được hợp tác với Quý công ty trong tương lai.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ JobLink",
                companyName
        );
    }
}
