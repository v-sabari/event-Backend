package com.example.Backend.dto.department;

import com.example.Backend.model.Department;

import java.time.Instant;

public class DepartmentResponseDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Long hodId;
    private String hodName;
    private boolean hodApprovalRequired;
    private Instant createdAt;
    private Instant updatedAt;

    public static DepartmentResponseDTO from(Department d) {
        DepartmentResponseDTO dto = new DepartmentResponseDTO();
        dto.id = d.getId();
        dto.name = d.getName();
        dto.code = d.getCode();
        dto.description = d.getDescription();
        dto.hodApprovalRequired = d.isHodApprovalRequired();
        dto.createdAt = d.getCreatedAt();
        dto.updatedAt = d.getUpdatedAt();
        if (d.getHod() != null) {
            dto.hodId = d.getHod().getId();
            dto.hodName = d.getHod().getName();
        }
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public Long getHodId() { return hodId; }
    public String getHodName() { return hodName; }
    public boolean isHodApprovalRequired() { return hodApprovalRequired; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
