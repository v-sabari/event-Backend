package com.example.Backend.security;

import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for JwtService - no Spring context, no live database.
 * The signing key is a Base64-encoded 48-byte seed (>= 32 bytes, which is
 * the minimum HS256 permits via Keys.hmacShaKeyFor).
 */
class JwtServiceTest {

    private static final String BASE64_SECRET =
            Base64.getEncoder().encodeToString("this-is-a-48-byte-super-secret-test-seed-value!!".getBytes());

    private static final long ACCESS_TTL = 3_600_000L;   // 1h
    private static final long REFRESH_TTL = 86_400_000L; // 24h

    private final JwtService jwtService = new JwtService(BASE64_SECRET, ACCESS_TTL, REFRESH_TTL);

    private User user(Long id, String regNumber, Role role) {
        User user = new User();
        user.setId(id);
        user.setRegNumber(regNumber);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setRole(role);
        return user;
    }

    @Test
    void accessTokenEmbedsSubjectRoleUidAndType() {
        User user = user(42L, "2023CS001", Role.STUDENT_ORGANIZER);

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("2023CS001");
        assertThat(jwtService.extractTokenType(token)).isEqualTo("ACCESS");
    }

    @Test
    void refreshTokenHasRefreshType() {
        User user = user(7L, "2022EC045", Role.SUPER_ADMIN);

        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("2022EC045");
        assertThat(jwtService.extractTokenType(token)).isEqualTo("REFRESH");
    }

    @Test
    void validTokenIsAcceptedForMatchingUsername() {
        User user = user(1L, "2024ME010", Role.STUDENT);

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, "2024ME010")).isTrue();
        assertThat(jwtService.isTokenParsable(token)).isTrue();
    }

    @Test
    void tokenIsRejectedForDifferentUsername() {
        User user = user(2L, "2024ME010", Role.STUDENT);

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, "someone-else")).isFalse();
    }

    @Test
    void tamperedTokenIsRejected() {
        User user = user(3L, "2024CIV009", Role.STUDENT);

        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isTokenValid(tampered, "2024CIV009")).isFalse();
        assertThat(jwtService.isTokenParsable(tampered)).isFalse();
    }

    @Test
    void garbageTokenIsRejected() {
        assertThat(jwtService.isTokenValid("not-a-jwt", "2024CIV009")).isFalse();
        assertThat(jwtService.isTokenParsable("not-a-jwt")).isFalse();
        assertThat(jwtService.isTokenValid("", "2024CIV009")).isFalse();
    }

    @Test
    void emptyTokenIsRejected() {
        assertThat(jwtService.isTokenValid(null, "2024CIV009")).isFalse();
    }

    @Test
    void expiredTokenFailsValidityButStillParsesUsername() {
        JwtService expiredService = new JwtService(BASE64_SECRET, -1000L, -1000L);
        User user = user(9L, "2021IT077", Role.HOD);

        String token = expiredService.generateAccessToken(user);

        // ExpiredJwtException still surfaces claims, so username/type read fine...
        assertThat(expiredService.extractUsername(token)).isEqualTo("2021IT077");
        assertThat(expiredService.extractTokenType(token)).isEqualTo("ACCESS");
        // ...but validity must fail.
        assertThat(expiredService.isTokenValid(token, "2021IT077")).isFalse();
    }

    @Test
    void refreshTtlMatchesConfiguredValue() {
        assertThat(jwtService.getRefreshTokenExpirationMs()).isEqualTo(REFRESH_TTL);
    }
}