package com.example.Backend.dto.notification;

import com.example.Backend.model.Notification;

import java.time.Instant;

public class NotificationResponseDTO {

    private Long id;
    private String title;
    private String message;
    private String type;
    private String relatedEntityType;
    private Long relatedEntityId;
    private boolean read;
    private Instant createdAt;

    public static NotificationResponseDTO from(Notification n) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.id = n.getId();
        dto.title = n.getTitle();
        dto.message = n.getMessage();
        dto.type = n.getType();
        dto.relatedEntityType = n.getRelatedEntityType();
        dto.relatedEntityId = n.getRelatedEntityId();
        dto.read = n.isRead();
        dto.createdAt = n.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}
