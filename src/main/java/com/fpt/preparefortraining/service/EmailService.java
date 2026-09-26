package com.fpt.preparefortraining.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {
    private final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    // mailSender: Cung cấp các hàm gửi mail cơ bản của Spring Boot
    private final JavaMailSender mailSender;
    
    // templateEngine: (Thymeleaf) dùng để render file HTML kết hợp với biến động
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    // Annotation @Async giúp hàm này chạy ngầm (background thread),
    // nhờ đó luồng đăng ký của người dùng không bị chậm lại khi chờ gửi mail.
    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            // Context là đối tượng chứa các biến truyền vào template HTML
            Context context = new Context();
            context.setVariable("name", name); // Truyền tên người dùng vào biến ${name}

            // Đọc file welcome-email.html trong thư mục resources/templates và nhúng biến vào
            String htmlContent = templateEngine.process("welcome-email", context);

            // MimeMessage hỗ trợ gửi mail dưới dạng HTML (thay vì plain text)
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail); // Người nhận
            helper.setSubject("Chào mừng đến với PrepareForTraining!"); // Tiêu đề mail
            helper.setText(htmlContent, true); // True = Kích hoạt chế độ đọc HTML

            mailSender.send(message); // Gửi mail đi
            log.info("Welcome email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            // Nếu có lỗi (VD: cấu hình sai, mạng lỗi), in ra console
            log.error("Failed to send welcome email to {}", toEmail, e);
        }
    }

    // Tương tự hàm trên, chạy ngầm để gửi mail reset password
    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetUrl) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetUrl", resetUrl); // Đường link chứa JWT Token

            // Process file reset-password-email.html
            String htmlContent = templateEngine.process("reset-password-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Yêu cầu đặt lại mật khẩu - PrepareForTraining");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }
}
