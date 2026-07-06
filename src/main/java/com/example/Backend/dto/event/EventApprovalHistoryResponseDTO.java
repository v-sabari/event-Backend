package com.example.Backend.dto.event;

import com.example.Backend.model.EventApprovalHistory;

import java.time.Instant;

public class EventApprovalHistoryResponseDTO {

    private Long id;
    private String fromStatus;
    private String toStatus;
    private Long actorId;
    private String actorName;
    private String remarks;
    private Instant createdAt;

    public static EventApprovalHistoryResponseDTO from(EventApprovalHistory h) {
        EventApprovalHistoryResponseDTO dto = new EventApprovalHistoryResponseDTO();
        dto.id = h.getId();
        dto.fromStatus = h.getFromStatus() != null ? h.getFromStatus().name() : null;
        dto.toStatus = h.getToStatus().name();
        dto.actorId = h.getActor().getId();
        dto.actorName = h.getActor().getName();
        dto.remarks = h.getRemarks();
        dto.createdAt = h.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public Long getActorId() { return actorId; }
    public String getActorName() { return actorName; }
    public String getRemarks() { return remarks; }
    public Instant getCreatedAt() { return createdAt; }
}
