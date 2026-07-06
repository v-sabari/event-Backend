package com.example.Backend.dto.registration;

import com.example.Backend.model.Registration;

import java.time.Instant;

public class RegistrationResponseDTO {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long userId;
    private String userName;
    private String userRegNumber;
    private String status;
    private String qrToken;
    private String qrDownloadUrl;
    private Instant checkedInAt;
    private Instant registeredAt;

    public static RegistrationResponseDTO from(Registration r) {
        RegistrationResponseDTO dto = new RegistrationResponseDTO();
        dto.id = r.getId();
        dto.eventId = r.getEvent().getId();
        dto.eventTitle = r.getEvent().getTitle();
        dto.userId = r.getUser().getId();
        dto.userName = r.getUser().getName();
        dto.userRegNumber = r.getUser().getRegNumber();
        dto.status = r.getStatus().name();
        dto.qrToken = r.getQrToken();
        dto.qrDownloadUrl = "/api/registrations/" + r.getId() + "/qr";
        dto.checkedInAt = r.getCheckedInAt();
        dto.registeredAt = r.getRegisteredAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserRegNumber() { return userRegNumber; }
    public String getStatus() { return status; }
    public String getQrToken() { return qrToken; }
    public String getQrDownloadUrl() { return qrDownloadUrl; }
    public Instant getCheckedInAt() { return checkedInAt; }
    public Instant getRegisteredAt() { return registeredAt; }
}
