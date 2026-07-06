package com.example.Backend.dto.venue;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VenueRequestDTO {

    @NotBlank(message = "Venue name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String location;

    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;
}
