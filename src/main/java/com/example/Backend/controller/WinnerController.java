package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.winner.WinnerRequestDTO;
import com.example.Backend.dto.winner.WinnerResponseDTO;
import com.example.Backend.model.User;
import com.example.Backend.model.Winner;
import com.example.Backend.service.WinnerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WinnerController {

    private final WinnerService winnerService;

    public WinnerController(WinnerService winnerService) {
        this.winnerService = winnerService;
    }

    @PostMapping("/api/events/{eventId}/winners")
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
    public ApiResponse<WinnerResponseDTO> addWinner(@PathVariable Long eventId,
                                                     @Valid @RequestBody WinnerRequestDTO dto,
                                                     @AuthenticationPrincipal User currentUser) {
        Winner winner = winnerService.addWinner(eventId, dto, currentUser);
        return ApiResponse.success("Winner added", WinnerResponseDTO.from(winner));
    }

    @DeleteMapping("/api/winners/{winnerId}")
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
    public ApiResponse<Void> removeWinner(@PathVariable Long winnerId, @AuthenticationPrincipal User currentUser) {
        winnerService.removeWinner(winnerId, currentUser);
        return ApiResponse.message("Winner removed");
    }

    // No role restriction (any authenticated user can view) - winner lists are for recognition, not privileged data.
    @GetMapping("/api/events/{eventId}/winners")
    public ApiResponse<List<WinnerResponseDTO>> list(@PathVariable Long eventId) {
        List<WinnerResponseDTO> response = winnerService.findByEvent(eventId).stream()
                .map(WinnerResponseDTO::from).toList();
        return ApiResponse.success(response);
    }
}
