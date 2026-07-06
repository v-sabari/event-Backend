package com.example.Backend.dto.winner;

import com.example.Backend.model.Winner;

public class WinnerResponseDTO {

    private Long id;
    private Long eventId;
    private Long userId;
    private String participantName;
    private String position;
    private String prize;

    public static WinnerResponseDTO from(Winner w) {
        WinnerResponseDTO dto = new WinnerResponseDTO();
        dto.id = w.getId();
        dto.eventId = w.getEvent().getId();
        dto.userId = w.getUser() != null ? w.getUser().getId() : null;
        dto.participantName = w.getParticipantName();
        dto.position = w.getPosition();
        dto.prize = w.getPrize();
        return dto;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public Long getUserId() { return userId; }
    public String getParticipantName() { return participantName; }
    public String getPosition() { return position; }
    public String getPrize() { return prize; }
}
