package com.example.Backend.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QrScanRequestDTO {

    @NotBlank(message = "QR token is required")
    private String qrToken;
}
