package com.example.Backend.service;

import com.example.Backend.exception.TooManyAttemptsException;

/**
 * BE-05: brute-force / credential-stuffing protection for /api/auth/login.
 * Tracks failed login attempts per client IP (broad protection against
 * distributed guessing across many accounts) and per account/regNumber
 * (targeted protection against repeated guessing on one known account),
 * and temporarily locks out whichever key crosses its failure threshold.
 */
public interface LoginAttemptService {

    /**
     * Call before doing any credential check. Throws if either the IP or
     * the account (when a regNumber is supplied) is currently locked out.
     */
    void assertNotBlocked(String clientIp, String regNumber) throws TooManyAttemptsException;

    /**
     * Record a failed attempt against the given client IP. Called on every
     * failed login, regardless of whether the regNumber corresponds to a
     * real account.
     */
    void recordIpFailure(String clientIp);

    /**
     * Record a failed attempt against a known account. Only call this when
     * the regNumber was confirmed to belong to an existing user, so an
     * attacker cannot grow the tracked-account set for free by submitting
     * arbitrary regNumbers.
     */
    void recordAccountFailure(String regNumber);

    /**
     * Clear failure counters for both the IP and the account after a
     * successful login.
     */
    void recordSuccess(String clientIp, String regNumber);
}