package com.example.Backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a caller has exceeded the allowed number of failed login
 * attempts (BE-05: brute-force / credential-stuffing protection on
 * /api/auth/login). Deliberately extends ApiException so it flows through
 * the existing GlobalExceptionHandler.handleApiException(...) path without
 * any changes needed there.
 */
public class TooManyAttemptsException extends ApiException {
    public TooManyAttemptsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}