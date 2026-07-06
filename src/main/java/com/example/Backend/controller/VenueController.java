package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.venue.VenueRequestDTO;
import com.example.Backend.dto.venue.VenueResponseDTO;
import com.example.Backend.model.Venue;
import com.example.Backend.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Venue master data. Writes restricted to SUPER_ADMIN/FACULTY_COORDINATOR
 * (whoever manages campus infrastructure); reads open to any authenticated
 * user since Student Organizers need the venue list while drafting an event.
 * Actual double-booking prevention lives in EventService (it needs Event
 * data, not just Venue data, to check for overlaps).
 */
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<VenueResponseDTO> create(@Valid @RequestBody VenueRequestDTO dto) {
        Venue created = venueService.create(dto);
        return ApiResponse.success("Venue created", VenueResponseDTO.from(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<VenueResponseDTO> update(@PathVariable Long id, @Valid @RequestBody VenueRequestDTO dto) {
        Venue updated = venueService.update(id, dto);
        return ApiResponse.success("Venue updated", VenueResponseDTO.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return ApiResponse.message("Venue deleted");
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<VenueResponseDTO> setActive(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(VenueResponseDTO.from(venueService.setActive(id, active)));
    }

    @GetMapping("/{id}")
    public ApiResponse<VenueResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(VenueResponseDTO.from(venueService.findById(id)));
    }

    @GetMapping
    public ApiResponse<List<VenueResponseDTO>> list() {
        List<VenueResponseDTO> response = venueService.findAll().stream().map(VenueResponseDTO::from).toList();
        return ApiResponse.success(response);
    }
}
