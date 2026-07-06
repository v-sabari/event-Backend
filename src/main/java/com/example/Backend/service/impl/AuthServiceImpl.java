package com.example.Backend.service.impl;

import com.example.Backend.dto.LoginDTO;
import com.example.Backend.dto.auth.AuthResponseDTO;
import com.example.Backend.exception.InvalidCredentialsException;
import com.example.Backend.exception.InvalidTokenException;
import com.example.Backend.model.RefreshToken;
import com.example.Backend.model.User;
import com.example.Backend.repository.RefreshTokenRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.security.JwtService;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Replaces the previous UserServiceImpl.login() implementation, which
 * compared passwords in plaintext and never issued any token. This is the
 * only class that knows how to turn a LoginDTO into an authenticated session.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(UserRepository userRepository,
                            RefreshTokenRepository refreshTokenRepository,
                            PasswordEncoder passwordEncoder,
                            JwtService jwtService,
                            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginDTO dto) {
        User user = userRepository.findByRegNumber(dto.getRegNumber())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid registration number or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid registration number or password");
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("This account has been disabled. Contact an administrator.");
        }

        log.info("User {} logged in", user.getRegNumber());
        auditLogService.record("LOGIN_SUCCESS", "User", user.getId(), "User logged in");

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO refresh(String rawRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not recognized"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked, please log in again");
        }

        if (!jwtService.isTokenParsable(rawRefreshToken)) {
            throw new InvalidTokenException("Refresh token is malformed");
        }

        User user = stored.getUser();

        // Rotate: revoke the used refresh token and issue a brand new pair.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        log.info("Refresh token rotated for user {}", user.getRegNumber());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByToken(rawRefreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            auditLogService.record("LOGOUT", "User", token.getUser().getId(), "User logged out");
        });
    }

    private AuthResponseDTO issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponseDTO(
                accessToken,
                refreshTokenValue,
                user.getId(),
                user.getName(),
                user.getRegNumber(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
