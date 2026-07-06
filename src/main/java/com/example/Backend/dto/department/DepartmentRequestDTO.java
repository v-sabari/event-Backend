package com.example.Backend.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequestDTO {

    @NotBlank(message = "Department name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(max = 20)
    private String code;

    @Size(max = 500)
    private String description;

    private Long hodId;

    private boolean hodApprovalRequired = false;
}
