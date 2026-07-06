package com.example.Backend.exception;

import org.springframework.http.HttpStatus;

public class FileStorageException extends ApiException {
    public FileStorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
