package com.example.Backend.controller;

import com.example.Backend.dto.LoginDTO;
import com.example.Backend.dto.auth.*;
import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.service.AuthService;
import com.example.Backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * Sole owner of /api/auth/**. This replaces both the old AuthController and
 * UserController, which previously both mapped POST /api/auth/login and
 * would have crashed the app at startup with an "ambiguous mapping" error.
 * CORS is handled centrally in SecurityConfig - no per-controller @CrossOrigin needed.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        return ApiResponse.success("Login successful", response);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        AuthResponseDTO response = authService.refresh(dto.getRefreshToken());
        return ApiResponse.success("Token refreshed", response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        authService.logout(dto.getRefreshToken());
        return ApiResponse.message("Logged out successfully");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        passwordResetService.initiateReset(dto.getEmail());
        // Same response whether or not the email exists, to avoid account enumeration.
        return ApiResponse.message("If an account exists for that email, an OTP has been sent");
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO dto) {
        passwordResetService.verifyOtp(dto.getEmail(), dto.getOtp());
        return ApiResponse.message("OTP verified");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO dto) {
        passwordResetService.resetPassword(dto.getEmail(), dto.getOtp(), dto.getNewPassword());
        return ApiResponse.message("Password reset successfully. Please log in with your new password");
    }
}
