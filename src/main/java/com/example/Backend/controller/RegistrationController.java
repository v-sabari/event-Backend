package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.registration.RegistrationResponseDTO;
import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.model.Registration;
import com.example.Backend.model.User;
import com.example.Backend.service.EventAuthorizationService;
import com.example.Backend.service.QrService;
import com.example.Backend.service.RegistrationService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Covers Student Registration (with Registration Deadline / Maximum
 * Participants / Waiting List enforced in the service) and the QR Entry
 * Pass image endpoint. QR *scanning* (attendance) lives in AttendanceController
 * since it's a distinct actor/action (staff scanning, not a student viewing their own pass).
 */
@RestController
public class RegistrationController {

    private final RegistrationService registrationService;
    private final EventAuthorizationService eventAuthorizationService;
    private final QrService qrService;

    public RegistrationController(RegistrationService registrationService,
                                  EventAuthorizationService eventAuthorizationService,
                                  QrService qrService) {
        this.registrationService = registrationService;
        this.eventAuthorizationService = eventAuthorizationService;
        this.qrService = qrService;
    }

    @PostMapping("/api/events/{eventId}/registrations")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<RegistrationResponseDTO> register(@PathVariable Long eventId, @AuthenticationPrincipal User currentUser) {
        Registration registration = registrationService.register(eventId, currentUser);
        String message = registration.getStatus().name().equals("WAITLISTED")
                ? "Event is full - you have been added to the waiting list"
                : "Registration confirmed";
        return ApiResponse.success(message, RegistrationResponseDTO.from(registration));
    }

    @DeleteMapping("/api/registrations/{id}")
    public ApiResponse<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        registrationService.cancel(id, currentUser);
        return ApiResponse.message("Registration cancelled");
    }

    @GetMapping("/api/registrations/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<RegistrationResponseDTO>> mine(@AuthenticationPrincipal User currentUser) {
        List<RegistrationResponseDTO> response = registrationService.findMine(currentUser).stream()
                .map(RegistrationResponseDTO::from).toList();
        return ApiResponse.success(response);
    }

    @GetMapping("/api/events/{eventId}/registrations")
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
    public ApiResponse<List<RegistrationResponseDTO>> roster(@PathVariable Long eventId,
                                                             @AuthenticationPrincipal User currentUser) {
        List<RegistrationResponseDTO> response = registrationService.findByEvent(eventId, currentUser).stream()
                .map(RegistrationResponseDTO::from).toList();
        return ApiResponse.success(response);
    }

    // QR Entry Pass - the registration's owner, or event-scoped staff (per
    // EventAuthorizationService), can fetch the PNG.
    @GetMapping(value = "/api/registrations/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrImage(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Registration registration = registrationService.findById(id);

        boolean isOwner = registration.getUser().getId().equals(currentUser.getId());
        boolean isEventStaff = eventAuthorizationService.canActOnEvent(registration.getEvent(), currentUser);
        if (!isOwner && !isEventStaff) {
            throw new AccessDeniedCustomException("You cannot view another student's entry pass");
        }

        return qrService.generateQrPng(registration.getQrToken(), 300);
    }
}