package com.example.Backend.controller;

import com.example.Backend.dto.club.ClubRequestDTO;
import com.example.Backend.dto.club.ClubResponseDTO;
import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.model.Club;
import com.example.Backend.service.ClubService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Clubs can be managed by SUPER_ADMIN and FACULTY_COORDINATOR (a faculty
 * coordinator is expected to be able to set up their own club without
 * needing a super admin for every change). Read access is open to all
 * authenticated users so students can browse clubs.
 */
@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<ClubResponseDTO> create(@Valid @RequestBody ClubRequestDTO dto) {
        Club created = clubService.create(dto);
        return ApiResponse.success("Club created", ClubResponseDTO.from(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<ClubResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ClubRequestDTO dto) {
        Club updated = clubService.update(id, dto);
        return ApiResponse.success("Club updated", ClubResponseDTO.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        clubService.delete(id);
        return ApiResponse.message("Club deleted");
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<ClubResponseDTO> setActive(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(ClubResponseDTO.from(clubService.setActive(id, active)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ClubResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(ClubResponseDTO.from(clubService.findById(id)));
    }

    @GetMapping
    public ApiResponse<List<ClubResponseDTO>> list() {
        List<ClubResponseDTO> response = clubService.findAll().stream().map(ClubResponseDTO::from).toList();
        return ApiResponse.success(response);
    }
}
