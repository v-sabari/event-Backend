package com.example.Backend.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedCustomException extends ApiException {
    public AccessDeniedCustomException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
