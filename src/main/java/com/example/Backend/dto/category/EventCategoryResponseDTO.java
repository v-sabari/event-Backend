package com.example.Backend.dto.category;

import com.example.Backend.model.EventCategory;

public class EventCategoryResponseDTO {

    private Long id;
    private String name;
    private String description;

    public static EventCategoryResponseDTO from(EventCategory c) {
        EventCategoryResponseDTO dto = new EventCategoryResponseDTO();
        dto.id = c.getId();
        dto.name = c.getName();
        dto.description = c.getDescription();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
