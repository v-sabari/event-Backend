package com.example.Backend.controller;

import com.example.Backend.dto.attendance.QrScanRequestDTO;
import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.registration.RegistrationResponseDTO;
import com.example.Backend.model.Registration;
import com.example.Backend.model.User;
import com.example.Backend.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * QR Attendance: staff at the venue scan a student's QR entry pass (decoded
 * client-side into its token string) and POST it here to mark attendance.
 * Deliberately restricted to organizer/faculty/admin roles - students cannot
 * check themselves in by hitting this endpoint with their own token.
 */
@RestController
@RequestMapping("/api/attendance")
@PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
public class AttendanceController {

    private final RegistrationService registrationService;

    public AttendanceController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/scan")
    public ApiResponse<RegistrationResponseDTO> scan(@Valid @RequestBody QrScanRequestDTO dto,
                                                       @AuthenticationPrincipal User scannedBy) {
        Registration registration = registrationService.checkInByQrToken(dto.getQrToken(), scannedBy);
        return ApiResponse.success("Attendance marked", RegistrationResponseDTO.from(registration));
    }
}
