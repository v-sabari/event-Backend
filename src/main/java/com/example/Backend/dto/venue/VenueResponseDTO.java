package com.example.Backend.dto.venue;

import com.example.Backend.model.Venue;

public class VenueResponseDTO {

    private Long id;
    private String name;
    private String location;
    private int capacity;
    private boolean active;

    public static VenueResponseDTO from(Venue v) {
        VenueResponseDTO dto = new VenueResponseDTO();
        dto.id = v.getId();
        dto.name = v.getName();
        dto.location = v.getLocation();
        dto.capacity = v.getCapacity();
        dto.active = v.isActive();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public boolean isActive() { return active; }
}
