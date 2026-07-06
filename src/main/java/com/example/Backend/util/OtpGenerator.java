package com.example.Backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private final int otpLength;
    private final SecureRandom random = new SecureRandom();

    public OtpGenerator(@Value("${app.otp.length}") int otpLength) {
        this.otpLength = otpLength;
    }

    public String generate() {
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
