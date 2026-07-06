package com.example.Backend.dto.winner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WinnerRequestDTO {

    private Long userId;

    @NotBlank(message = "Participant name is required")
    @Size(max = 200)
    private String participantName;

    @NotBlank(message = "Position is required")
    @Size(max = 50)
    private String position;

    @Size(max = 255)
    private String prize;
}
