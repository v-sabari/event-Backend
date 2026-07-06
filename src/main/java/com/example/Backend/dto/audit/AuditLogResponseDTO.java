package com.example.Backend.dto.audit;

import com.example.Backend.model.AuditLog;

import java.time.Instant;

public class AuditLogResponseDTO {

    private Long id;
    private String performedBy;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private Instant createdAt;

    public static AuditLogResponseDTO from(AuditLog log) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.id = log.getId();
        dto.performedBy = log.getPerformedBy();
        dto.action = log.getAction();
        dto.entityType = log.getEntityType();
        dto.entityId = log.getEntityId();
        dto.details = log.getDetails();
        dto.createdAt = log.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getPerformedBy() { return performedBy; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
