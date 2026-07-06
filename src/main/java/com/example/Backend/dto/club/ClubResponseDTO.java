package com.example.Backend.dto.club;

import com.example.Backend.model.Club;

import java.time.Instant;

public class ClubResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Long departmentId;
    private String departmentName;
    private Long coordinatorId;
    private String coordinatorName;
    private boolean active;
    private Instant createdAt;

    public static ClubResponseDTO from(Club c) {
        ClubResponseDTO dto = new ClubResponseDTO();
        dto.id = c.getId();
        dto.name = c.getName();
        dto.description = c.getDescription();
        dto.active = c.isActive();
        dto.createdAt = c.getCreatedAt();
        if (c.getDepartment() != null) {
            dto.departmentId = c.getDepartment().getId();
            dto.departmentName = c.getDepartment().getName();
        }
        if (c.getCoordinator() != null) {
            dto.coordinatorId = c.getCoordinator().getId();
            dto.coordinatorName = c.getCoordinator().getName();
        }
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public Long getCoordinatorId() { return coordinatorId; }
    public String getCoordinatorName() { return coordinatorName; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
