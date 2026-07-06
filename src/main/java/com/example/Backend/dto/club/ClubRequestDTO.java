package com.example.Backend.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClubRequestDTO {

    @NotBlank(message = "Club name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    private Long departmentId;

    private Long coordinatorId;
}
