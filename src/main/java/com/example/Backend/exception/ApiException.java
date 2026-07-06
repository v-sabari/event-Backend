package com.example.Backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for all application-specific exceptions.
 * Every subclass carries its own HttpStatus so the GlobalExceptionHandler
 * can translate it into a consistent HTTP response without any per-exception
 * if/else branching.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
