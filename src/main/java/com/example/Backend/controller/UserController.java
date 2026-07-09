package com.example.Backend.controller;

import com.example.Backend.dto.auth.RegisterRequestDTO;
import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.user.UserResponseDTO;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Owns /api/users/**: the authenticated user's own profile, plus
 * admin/faculty-coordinator-driven account provisioning and management.
 * No longer duplicates the login endpoint that used to live here.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponseDTO> me(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(UserResponseDTO.from(currentUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        User created = userService.register(dto);
        return ApiResponse.success("User created", UserResponseDTO.from(created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<Page<UserResponseDTO>> list(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<User> users = role != null
                ? userService.findByRole(role, PageRequest.of(page, size))
                : userService.findAll(PageRequest.of(page, size));
        Page<UserResponseDTO> response = users.map(UserResponseDTO::from);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<UserResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(UserResponseDTO.from(userService.findById(id)));
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<UserResponseDTO> setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        return ApiResponse.success(UserResponseDTO.from(userService.setEnabled(id, enabled)));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<UserResponseDTO> changeRole(@PathVariable Long id, @RequestParam Role role) {
        return ApiResponse.success(UserResponseDTO.from(userService.changeRole(id, role)));
    }
}