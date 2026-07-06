package com.example.Backend.service.impl;

import com.example.Backend.exception.OtpException;
import com.example.Backend.model.PasswordResetOtp;
import com.example.Backend.model.User;
import com.example.Backend.repository.PasswordResetOtpRepository;
import com.example.Backend.repository.RefreshTokenRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.EmailService;
import com.example.Backend.service.PasswordResetService;
import com.example.Backend.util.OtpGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpGenerator otpGenerator;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final int otpExpiryMinutes;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                     PasswordResetOtpRepository otpRepository,
                                     RefreshTokenRepository refreshTokenRepository,
                                     PasswordEncoder passwordEncoder,
                                     OtpGenerator otpGenerator,
                                     EmailService emailService,
                                     AuditLogService auditLogService,
                                     @Value("${app.otp.expiry-minutes}") int otpExpiryMinutes) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpGenerator = otpGenerator;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
        this.otpExpiryMinutes = otpExpiryMinutes;
    }

    @Override
    @Transactional
    public void initiateReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        // Deliberately do not throw ResourceNotFoundException here: revealing
        // whether an email exists in the system is an account-enumeration risk.
        // The endpoint always responds the same way regardless.
        if (userOpt.isEmpty()) {
            log.info("Password reset requested for unknown email (no-op)");
            return;
        }

        User user = userOpt.get();
        String otp = otpGenerator.generate();

        PasswordResetOtp entity = new PasswordResetOtp();
        entity.setUser(user);
        entity.setOtpHash(passwordEncoder.encode(otp));
        entity.setExpiresAt(Instant.now().plusSeconds(otpExpiryMinutes * 60L));
        otpRepository.save(entity);

        emailService.sendOtpEmail(user.getEmail(), otp, otpExpiryMinutes);
        log.info("Password reset OTP issued for user {}", user.getRegNumber());
        auditLogService.record("PASSWORD_RESET_REQUESTED", "User", user.getId(), "OTP issued");
    }

    @Override
    public void verifyOtp(String email, String otp) {
        findValidOtp(email, otp);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OtpException("Invalid or expired OTP"));

        PasswordResetOtp validOtp = findValidOtp(email, otp);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        validOtp.setUsed(true);
        otpRepository.save(validOtp);

        // Force re-login everywhere after a password reset.
        refreshTokenRepository.revokeAllByUser(user);

        log.info("Password reset completed for user {}", user.getRegNumber());
        auditLogService.record("PASSWORD_RESET_COMPLETED", "User", user.getId(), "Password changed via OTP");
    }

    private PasswordResetOtp findValidOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OtpException("Invalid or expired OTP"));

        List<PasswordResetOtp> candidates = otpRepository.findByUserAndUsedFalseOrderByCreatedAtDesc(user);

        return candidates.stream()
                .filter(o -> o.getExpiresAt().isAfter(Instant.now()))
                .filter(o -> passwordEncoder.matches(otp, o.getOtpHash()))
                .findFirst()
                .orElseThrow(() -> new OtpException("Invalid or expired OTP"));
    }
}
