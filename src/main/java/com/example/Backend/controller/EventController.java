package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.event.ApprovalActionDTO;
import com.example.Backend.dto.event.EventApprovalHistoryResponseDTO;
import com.example.Backend.dto.event.EventRequestDTO;
import com.example.Backend.dto.event.EventResponseDTO;
import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.model.Event;
import com.example.Backend.model.EventStatus;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.service.EventApprovalService;
import com.example.Backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces the empty EventController stub from Phase 1/2. Covers both
 * "Event Creation" (draft CRUD) and "Event Approval Workflow" (submit /
 * approve / reject / history) since they operate on the same resource.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final EventApprovalService approvalService;

    public EventController(EventService eventService, EventApprovalService approvalService) {
        this.eventService = eventService;
        this.approvalService = approvalService;
    }

    // ---- Event Creation ----

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'SUPER_ADMIN')")
    public ApiResponse<EventResponseDTO> createDraft(@Valid @RequestBody EventRequestDTO dto,
                                                      @AuthenticationPrincipal User currentUser) {
        Event created = eventService.createDraft(dto, currentUser);
        return ApiResponse.success("Event draft created", EventResponseDTO.from(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'SUPER_ADMIN')")
    public ApiResponse<EventResponseDTO> updateDraft(@PathVariable Long id,
                                                      @Valid @RequestBody EventRequestDTO dto,
                                                      @AuthenticationPrincipal User currentUser) {
        Event updated = eventService.updateDraft(id, dto, currentUser);
        return ApiResponse.success("Event draft updated", EventResponseDTO.from(updated));
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponseDTO> getById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Event event = eventService.findById(id);
        assertCanView(event, currentUser);
        return ApiResponse.success(EventResponseDTO.from(event));
    }

    @GetMapping
    public ApiResponse<List<EventResponseDTO>> myVisibleEvents(@AuthenticationPrincipal User currentUser) {
        List<EventResponseDTO> response = eventService.findVisibleTo(currentUser).stream()
                .map(EventResponseDTO::from).toList();
        return ApiResponse.success(response);
    }

    // Public calendar / listing (Event Calendar module) - no login required, published events only.
    @GetMapping("/published")
    public ApiResponse<List<EventResponseDTO>> published() {
        List<EventResponseDTO> response = eventService.findPublished().stream()
                .map(EventResponseDTO::from).toList();
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'SUPER_ADMIN')")
    public ApiResponse<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        eventService.cancel(id, currentUser);
        return ApiResponse.message("Event cancelled");
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'SUPER_ADMIN')")
    public ApiResponse<Void> complete(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        eventService.markCompleted(id, currentUser);
        return ApiResponse.message("Event marked as completed");
    }

    // ---- Event Approval Workflow ----

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'SUPER_ADMIN')")
    public ApiResponse<EventResponseDTO> submit(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Event event = approvalService.submit(id, currentUser);
        return ApiResponse.success("Event submitted for approval", EventResponseDTO.from(event));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
    public ApiResponse<EventResponseDTO> approve(@PathVariable Long id,
                                                  @Valid @RequestBody(required = false) ApprovalActionDTO dto,
                                                  @AuthenticationPrincipal User currentUser) {
        String remarks = dto != null ? dto.getRemarks() : null;
        Event event = approvalService.approve(id, currentUser, remarks);
        return ApiResponse.success("Event approved", EventResponseDTO.from(event));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
    public ApiResponse<EventResponseDTO> reject(@PathVariable Long id,
                                                 @Valid @RequestBody ApprovalActionDTO dto,
                                                 @AuthenticationPrincipal User currentUser) {
        Event event = approvalService.reject(id, currentUser, dto.getRemarks());
        return ApiResponse.success("Event rejected", EventResponseDTO.from(event));
    }

    @GetMapping("/{id}/history")
    public ApiResponse<List<EventApprovalHistoryResponseDTO>> history(@PathVariable Long id) {
        List<EventApprovalHistoryResponseDTO> response = approvalService.getHistory(id).stream()
                .map(EventApprovalHistoryResponseDTO::from).toList();
        return ApiResponse.success(response);
    }

    @GetMapping("/pending-approval")
    @PreAuthorize("hasAnyRole('FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
    public ApiResponse<List<EventResponseDTO>> pendingApproval(@AuthenticationPrincipal User currentUser) {
        List<EventResponseDTO> response = approvalService.findPendingFor(currentUser).stream()
                .map(EventResponseDTO::from).toList();
        return ApiResponse.success(response);
    }

    // Search & Filters module - public, mirrors /published but with optional filters.
    @GetMapping("/search")
    public ApiResponse<List<EventResponseDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date
    ) {
        List<EventResponseDTO> response = eventService.search(name, departmentId, categoryId, venueId, date).stream()
                .map(EventResponseDTO::from).toList();
        return ApiResponse.success(response);
    }

    private void assertCanView(Event event, User currentUser) {
        boolean isPublic = event.getStatus() == EventStatus.PUBLISHED || event.getStatus() == EventStatus.COMPLETED;
        boolean isOwner = event.getCreatedBy().getId().equals(currentUser.getId());
        boolean isPrivileged = currentUser.getRole() == Role.SUPER_ADMIN
                || currentUser.getRole() == Role.FACULTY_COORDINATOR
                || currentUser.getRole() == Role.HOD;
        if (!isPublic && !isOwner && !isPrivileged) {
            throw new AccessDeniedCustomException("You do not have permission to view this event");
        }
    }
}
