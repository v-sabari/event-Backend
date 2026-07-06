package com.example.Backend.service;

public interface PasswordResetService {

    /** Generates an OTP, stores its hash, and emails it to the user (if the email exists). */
    void initiateReset(String email);

    /** Verifies an OTP without consuming it - used for a "verify OTP" UI step before showing the reset form. */
    void verifyOtp(String email, String otp);

    /** Verifies the OTP, consumes it, and updates the user's password. */
    void resetPassword(String email, String otp, String newPassword);
}
