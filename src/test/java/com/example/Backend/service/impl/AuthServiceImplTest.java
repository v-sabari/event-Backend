package com.example.Backend.service.impl;

import com.example.Backend.dto.LoginDTO;
import com.example.Backend.dto.auth.AuthResponseDTO;
import com.example.Backend.exception.InvalidTokenException;
import com.example.Backend.model.RefreshToken;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.repository.RefreshTokenRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.security.JwtService;
import com.example.Backend.security.RefreshTokenHasher;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for AuthServiceImpl - focuses on the refresh-token security
 * contract: only a hash of the token is persisted, rotation revokes the used
 * token, and a replayed/revoked token revokes the whole token family.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginPersistsOnlyTheTokenHash() {
        User user = user();
        LoginDTO dto = new LoginDTO();
        dto.setRegNumber("2023CS001");
        dto.setPassword("password123");

        when(userRepository.findByRegNumber("2023CS001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("raw.refresh.token");
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(60000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDTO response = authService.login(dto, "127.0.0.1");

        assertThat(response.getRefreshToken()).isEqualTo("raw.refresh.token");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken persisted = captor.getValue();
        assertThat(persisted.getToken()).isEqualTo(RefreshTokenHasher.hash("raw.refresh.token"));
        assertThat(persisted.getToken()).isNotEqualTo("raw.refresh.token");
    }

    @Test
    void refreshRotatesUsedTokenAndIssuesNewPair() {
        User user = user();
        String raw = "old.refresh.token";

        RefreshToken stored = new RefreshToken();
        stored.setId(1L);
        stored.setToken(RefreshTokenHasher.hash(raw));
        stored.setUser(user);
        stored.setExpiresAt(Instant.now().plusSeconds(3600));
        stored.setRevoked(false);

        when(refreshTokenRepository.findByToken(RefreshTokenHasher.hash(raw)))
                .thenReturn(Optional.of(stored));
        when(jwtService.isTokenParsable(raw)).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new.refresh.token");
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(60000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDTO response = authService.refresh(raw);

        assertThat(response.getRefreshToken()).isEqualTo("new.refresh.token");
        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void refreshRejectsReplayedTokenAndRevokesFamily() {
        User user = user();
        String raw = "stolen.refresh.token";

        RefreshToken stored = new RefreshToken();
        stored.setId(2L);
        stored.setToken(RefreshTokenHasher.hash(raw));
        stored.setUser(user);
        stored.setExpiresAt(Instant.now().plusSeconds(3600));
        stored.setRevoked(true);

        when(refreshTokenRepository.findByToken(RefreshTokenHasher.hash(raw)))
                .thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(raw))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("already used");

        verify(refreshTokenRepository).revokeAllByUser(user);
    }

    @Test
    void refreshRejectsExpiredToken() {
        User user = user();
        String raw = "expired.refresh.token";

        RefreshToken stored = new RefreshToken();
        stored.setToken(RefreshTokenHasher.hash(raw));
        stored.setUser(user);
        stored.setExpiresAt(Instant.now().minusSeconds(60));
        stored.setRevoked(false);

        when(refreshTokenRepository.findByToken(RefreshTokenHasher.hash(raw)))
                .thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(raw))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void refreshRejectsUnknownToken() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown.token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("not recognized");
    }

    @Test
    void logoutRevokesTokenByHash() {
        User user = user();
        String raw = "logout.refresh.token";

        RefreshToken stored = new RefreshToken();
        stored.setToken(RefreshTokenHasher.hash(raw));
        stored.setUser(user);
        stored.setExpiresAt(Instant.now().plusSeconds(3600));
        stored.setRevoked(false);

        when(refreshTokenRepository.findByToken(RefreshTokenHasher.hash(raw)))
                .thenReturn(Optional.of(stored));

        authService.logout(raw);

        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(stored);
        verify(auditLogService).record(org.mockito.ArgumentMatchers.eq("LOGOUT"),
                org.mockito.ArgumentMatchers.eq("User"),
                org.mockito.ArgumentMatchers.eq(user.getId()),
                anyString());
    }

    private User user() {
        User user = new User();
        user.setId(7L);
        user.setRegNumber("2023CS001");
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPassword("$2a$encoded-hash");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        return user;
    }
}