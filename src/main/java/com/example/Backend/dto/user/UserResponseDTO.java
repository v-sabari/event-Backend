package com.example.Backend.dto.user;

import com.example.Backend.model.User;

/**
 * Safe outward-facing view of a User - deliberately excludes the password
 * hash, which the raw entity would otherwise expose via Jackson serialization.
 */
public class UserResponseDTO {

    private Long id;
    private String regNumber;
    private String name;
    private String email;
    private String role;
    private Long departmentId;
    private String departmentName;
    private boolean enabled;

    public static UserResponseDTO from(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.id = user.getId();
        dto.regNumber = user.getRegNumber();
        dto.name = user.getName();
        dto.email = user.getEmail();
        dto.role = user.getRole().name();
        dto.enabled = user.isEnabled();
        if (user.getDepartment() != null) {
            dto.departmentId = user.getDepartment().getId();
            dto.departmentName = user.getDepartment().getName();
        }
        return dto;
    }

    public Long getId() { return id; }
    public String getRegNumber() { return regNumber; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public boolean isEnabled() { return enabled; }
}
