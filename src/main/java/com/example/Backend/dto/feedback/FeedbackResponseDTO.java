package com.example.Backend.dto.feedback;

import com.example.Backend.model.Feedback;

import java.time.Instant;

public class FeedbackResponseDTO {

    private Long id;
    private Long eventId;
    private Long userId;
    private String userName;
    private int rating;
    private String comments;
    private Instant createdAt;

    public static FeedbackResponseDTO from(Feedback f) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.id = f.getId();
        dto.eventId = f.getEvent().getId();
        dto.userId = f.getUser().getId();
        dto.userName = f.getUser().getName();
        dto.rating = f.getRating();
        dto.comments = f.getComments();
        dto.createdAt = f.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getRating() { return rating; }
    public String getComments() { return comments; }
    public Instant getCreatedAt() { return createdAt; }
}
