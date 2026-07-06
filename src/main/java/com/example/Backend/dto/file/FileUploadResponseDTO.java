package com.example.Backend.dto.file;

import com.example.Backend.model.UploadedFile;

public class FileUploadResponseDTO {

    private Long id;
    private String originalFileName;
    private String storedFileName;
    private String contentType;
    private long sizeBytes;
    private String downloadUrl;

    public static FileUploadResponseDTO from(UploadedFile file) {
        FileUploadResponseDTO dto = new FileUploadResponseDTO();
        dto.id = file.getId();
        dto.originalFileName = file.getOriginalFileName();
        dto.storedFileName = file.getStoredFileName();
        dto.contentType = file.getContentType();
        dto.sizeBytes = file.getSizeBytes();
        dto.downloadUrl = "/api/files/download/" + file.getStoredFileName();
        return dto;
    }

    public Long getId() { return id; }
    public String getOriginalFileName() { return originalFileName; }
    public String getStoredFileName() { return storedFileName; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getDownloadUrl() { return downloadUrl; }
}
