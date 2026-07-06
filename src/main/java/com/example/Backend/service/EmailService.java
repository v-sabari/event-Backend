package com.example.Backend.service;

public interface EmailService {

    void sendOtpEmail(String toEmail, String otp, int expiryMinutes);

    /** Generic notification email, reused by NotificationService for approval/registration/certificate emails. */
    void sendEmail(String toEmail, String subject, String body);
}
