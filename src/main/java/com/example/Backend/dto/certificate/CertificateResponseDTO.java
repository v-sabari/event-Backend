package com.example.Backend.dto.certificate;

import com.example.Backend.model.Certificate;

import java.time.Instant;

public class CertificateResponseDTO {

    private Long id;
    private Long registrationId;
    private String certificateCode;
    private String downloadUrl;
    private Instant generatedAt;

    public static CertificateResponseDTO from(Certificate c) {
        CertificateResponseDTO dto = new CertificateResponseDTO();
        dto.id = c.getId();
        dto.registrationId = c.getRegistration().getId();
        dto.certificateCode = c.getCertificateCode();
        dto.downloadUrl = "/api/files/download/" + c.getStoredFileName();
        dto.generatedAt = c.getGeneratedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getRegistrationId() { return registrationId; }
    public String getCertificateCode() { return certificateCode; }
    public String getDownloadUrl() { return downloadUrl; }
    public Instant getGeneratedAt() { return generatedAt; }
}
