package com.example.Backend.service.impl;

import com.example.Backend.exception.TooManyAttemptsException;
import com.example.Backend.service.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BE-05: in-memory brute-force / credential-stuffing protection for
 * /api/auth/login. Deliberately kept dependency-free (no Bucket4j/Redis) -
 * this pom.xml has no rate-limiting library on the classpath, and a
 * single-instance deployment (this app has no clustering/session-affinity
 * setup anywhere else in the codebase either) doesn't need a distributed
 * store for this to be effective.
 *
 * Two independent tracks:
 *  - Per client IP: catches an attacker (or script) hammering many
 *    different regNumbers from one source.
 *  - Per account (regNumber): catches an attacker rotating source IPs
 *    against one known/targeted account. Only ever incremented for
 *    regNumbers confirmed to belong to a real user (see
 *    recordAccountFailure javadoc) so this map can't be grown for free.
 *
 * Note on IP resolution: the caller (AuthController) passes
 * HttpServletRequest#getRemoteAddr(), not an X-Forwarded-For header. This
 * codebase has no trusted-proxy configuration (no
 * server.forward-headers-strategy, no RemoteIpValve) anywhere, so trusting
 * a client-supplied header here would let an attacker forge a new "IP" on
 * every request and bypass this protection entirely. If this app is ever
 * deployed behind a reverse proxy that should be trusted for the real
 * client IP, that needs to be configured explicitly (and validated) before
 * switching the IP source - that is a separate, deliberate change and is
 * not assumed here.
 */
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptServiceImpl.class);
    private static final String GENERIC_MESSAGE =
            "Too many failed login attempts. Please try again later.";

    private final int maxIpAttempts;
    private final int maxAccountAttempts;
    private final Duration window;
    private final Duration lockout;

    private final ConcurrentHashMap<String, AttemptWindow> ipAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptWindow> accountAttempts = new ConcurrentHashMap<>();

    public LoginAttemptServiceImpl(
            @Value("${app.security.login.max-ip-attempts:30}") int maxIpAttempts,
            @Value("${app.security.login.max-account-attempts:5}") int maxAccountAttempts,
            @Value("${app.security.login.window-minutes:15}") long windowMinutes,
            @Value("${app.security.login.lockout-minutes:15}") long lockoutMinutes) {
        this.maxIpAttempts = maxIpAttempts;
        this.maxAccountAttempts = maxAccountAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
        this.lockout = Duration.ofMinutes(lockoutMinutes);
    }

    @Override
    public void assertNotBlocked(String clientIp, String regNumber) {
        if (clientIp != null && isBlocked(ipAttempts.get(clientIp))) {
            log.warn("Login blocked: IP {} is temporarily locked out", clientIp);
            throw new TooManyAttemptsException(GENERIC_MESSAGE);
        }
        if (regNumber != null && isBlocked(accountAttempts.get(regNumber))) {
            log.warn("Login blocked: account {} is temporarily locked out", regNumber);
            throw new TooManyAttemptsException(GENERIC_MESSAGE);
        }
    }

    @Override
    public void recordIpFailure(String clientIp) {
        if (clientIp == null) return;
        registerFailure(ipAttempts, clientIp, maxIpAttempts, "IP");
    }

    @Override
    public void recordAccountFailure(String regNumber) {
        if (regNumber == null) return;
        registerFailure(accountAttempts, regNumber, maxAccountAttempts, "account");
    }

    @Override
    public void recordSuccess(String clientIp, String regNumber) {
        if (clientIp != null) ipAttempts.remove(clientIp);
        if (regNumber != null) accountAttempts.remove(regNumber);
    }

    private void registerFailure(ConcurrentHashMap<String, AttemptWindow> store, String key, int maxAttempts, String label) {
        AttemptWindow attempt = store.computeIfAbsent(key, k -> new AttemptWindow());
        boolean justLocked;
        synchronized (attempt) {
            Instant now = Instant.now();
            // Rolling window: if the current window has expired (and we're not
            // already in an active lockout), old failures no longer count.
            if (attempt.windowStart == null || now.isAfter(attempt.windowStart.plus(window))) {
                attempt.windowStart = now;
                attempt.count = 0;
            }
            attempt.count++;
            justLocked = attempt.count >= maxAttempts && attempt.lockedUntil == null;
            if (justLocked) {
                attempt.lockedUntil = now.plus(lockout);
            }
        }
        if (justLocked) {
            log.warn("Login {} {} locked out for {} after {} failed attempts", label, key, lockout, attempt.count);
        }
    }

    private boolean isBlocked(AttemptWindow attempt) {
        if (attempt == null) return false;
        synchronized (attempt) {
            Instant now = Instant.now();
            if (attempt.lockedUntil != null) {
                if (now.isBefore(attempt.lockedUntil)) {
                    return true;
                }
                // Lockout expired - clear it so the account/IP can try again.
                attempt.lockedUntil = null;
                attempt.count = 0;
                attempt.windowStart = now;
            }
            return false;
        }
    }

    /**
     * Periodic sweep so this map doesn't grow without bound over the life of
     * a long-running instance. Anything whose window and (if present) lockout
     * have both fully expired is safe to drop entirely.
     */
    @Scheduled(fixedRate = 10 * 60 * 1000L)
    void cleanupExpiredEntries() {
        Instant now = Instant.now();
        ipAttempts.entrySet().removeIf(e -> isExpired(e.getValue(), now));
        accountAttempts.entrySet().removeIf(e -> isExpired(e.getValue(), now));
    }

    private boolean isExpired(AttemptWindow attempt, Instant now) {
        synchronized (attempt) {
            if (attempt.lockedUntil != null) {
                return now.isAfter(attempt.lockedUntil);
            }
            return attempt.windowStart == null || now.isAfter(attempt.windowStart.plus(window));
        }
    }

    private static final class AttemptWindow {
        private int count = 0;
        private Instant windowStart;
        private Instant lockedUntil;
    }
}