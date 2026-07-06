package com.example.Backend.dto.event;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Used for both "create draft" and "update draft" - an Event can only be
 * edited while it is in DRAFT or REJECTED status (enforced in the service),
 * so one request shape covers both actions.
 */
@Data
public class EventRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    private Long categoryId;

    private Long clubId;

    private Long departmentId;

    @NotNull(message = "Venue is required")
    private Long venueId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    @NotNull(message = "Registration deadline is required")
    private Instant registrationDeadline;

    @Min(value = 1, message = "Maximum participants must be at least 1")
    private int maxParticipants;

    @DecimalMin(value = "0.0", message = "Fee cannot be negative")
    private BigDecimal fee = BigDecimal.ZERO;

    private String bannerUrl;
}
