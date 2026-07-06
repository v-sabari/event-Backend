package com.example.Backend.dto.gallery;

import com.example.Backend.model.GalleryImage;

import java.time.Instant;

public class GalleryImageResponseDTO {

    private Long id;
    private Long eventId;
    private String caption;
    private String imageUrl;
    private Long uploadedById;
    private String uploadedByName;
    private Instant createdAt;

    public static GalleryImageResponseDTO from(GalleryImage g) {
        GalleryImageResponseDTO dto = new GalleryImageResponseDTO();
        dto.id = g.getId();
        dto.eventId = g.getEvent().getId();
        dto.caption = g.getCaption();
        dto.imageUrl = "/api/files/download/" + g.getUploadedFile().getStoredFileName();
        dto.uploadedById = g.getUploadedBy().getId();
        dto.uploadedByName = g.getUploadedBy().getName();
        dto.createdAt = g.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public String getCaption() { return caption; }
    public String getImageUrl() { return imageUrl; }
    public Long getUploadedById() { return uploadedById; }
    public String getUploadedByName() { return uploadedByName; }
    public Instant getCreatedAt() { return createdAt; }
}
