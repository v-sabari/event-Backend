package com.example.Backend.exception;

import org.springframework.http.HttpStatus;

public class OtpException extends ApiException {
    public OtpException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
