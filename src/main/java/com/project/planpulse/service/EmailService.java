package com.project.planpulse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Value("${app.reset-password-url}")
    private String resetPasswordUrl; // <web-url>/reset-password?token=

    @Value("${spring.mail.username}")
    private String username;

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String token) throws RuntimeException {
        try {
            String resetLink = resetPasswordUrl + token;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(username);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request");
            message.setText("You requested a password reset. Click the link to reset your password: " + resetLink);
            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
