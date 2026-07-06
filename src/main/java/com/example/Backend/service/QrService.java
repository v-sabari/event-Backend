package com.example.Backend.service;

/**
 * Pure QR-image generation, kept separate from RegistrationService so the
 * QR Attendance module (scanning) doesn't need to depend on registration
 * business rules, and so this could be reused for other QR needs later.
 */
public interface QrService {

    /** Returns PNG image bytes encoding the given payload (typically a Registration's qrToken). */
    byte[] generateQrPng(String payload, int sizePx);
}
