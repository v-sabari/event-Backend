package com.example.Backend.dto.event;

import com.example.Backend.model.Event;

import java.math.BigDecimal;
import java.time.Instant;

public class EventResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long clubId;
    private String clubName;
    private Long departmentId;
    private String departmentName;
    private Long venueId;
    private String venueName;
    private Instant startTime;
    private Instant endTime;
    private Instant registrationDeadline;
    private int maxParticipants;
    private BigDecimal fee;
    private String bannerUrl;
    private String status;
    private Long createdById;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;

    public static EventResponseDTO from(Event e) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.id = e.getId();
        dto.title = e.getTitle();
        dto.description = e.getDescription();
        dto.venueId = e.getVenue().getId();
        dto.venueName = e.getVenue().getName();
        dto.startTime = e.getStartTime();
        dto.endTime = e.getEndTime();
        dto.registrationDeadline = e.getRegistrationDeadline();
        dto.maxParticipants = e.getMaxParticipants();
        dto.fee = e.getFee();
        dto.bannerUrl = e.getBannerUrl();
        dto.status = e.getStatus().name();
        dto.createdById = e.getCreatedBy().getId();
        dto.createdByName = e.getCreatedBy().getName();
        dto.createdAt = e.getCreatedAt();
        dto.updatedAt = e.getUpdatedAt();
        if (e.getCategory() != null) {
            dto.categoryId = e.getCategory().getId();
            dto.categoryName = e.getCategory().getName();
        }
        if (e.getClub() != null) {
            dto.clubId = e.getClub().getId();
            dto.clubName = e.getClub().getName();
        }
        if (e.getDepartment() != null) {
            dto.departmentId = e.getDepartment().getId();
            dto.departmentName = e.getDepartment().getName();
        }
        return dto;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Long getClubId() { return clubId; }
    public String getClubName() { return clubName; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public Long getVenueId() { return venueId; }
    public String getVenueName() { return venueName; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Instant getRegistrationDeadline() { return registrationDeadline; }
    public int getMaxParticipants() { return maxParticipants; }
    public BigDecimal getFee() { return fee; }
    public String getBannerUrl() { return bannerUrl; }
    public String getStatus() { return status; }
    public Long getCreatedById() { return createdById; }
    public String getCreatedByName() { return createdByName; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
