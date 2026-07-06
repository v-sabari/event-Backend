package com.example.Backend.service.impl;

import com.example.Backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender, @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp, int expiryMinutes) {
        sendEmail(
                toEmail,
                "Campus Connect - Password Reset OTP",
                "Your password reset OTP is: " + otp + "\n\n" +
                "This code expires in " + expiryMinutes + " minutes.\n" +
                "If you did not request a password reset, you can safely ignore this email."
        );
    }

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Email sent to {} - subject: {}", maskEmail(toEmail), subject);
        } catch (Exception e) {
            // Logged, not thrown: notification-triggering actions (approval, registration, etc.)
            // should still succeed even if the mail server is unreachable.
            log.error("Failed to send email to {}: {}", maskEmail(toEmail), e.getMessage());
        }
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(at, 0));
        return email.charAt(0) + "***" + email.substring(at);
    }
}
