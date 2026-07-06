package com.example.Backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Used by SUPER_ADMIN / FACULTY_COORDINATOR to provision new accounts.
 * There is intentionally no public self-signup endpoint yet - account
 * creation for a college system is expected to be admin/faculty driven.
 */
@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Registration number is required")
    private String regNumber;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Role is required")
    private String role;

    private Long departmentId;
}
