package com.example.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "Registration number is required")
    private String regNumber;

    @NotBlank(message = "Password is required")
    private String password;

}