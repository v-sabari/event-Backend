package com.example.Backend.controller;

import com.example.Backend.dto.certificate.CertificateResponseDTO;
import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.model.Certificate;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.service.CertificateService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Certificate generation is staff-triggered (organizer/faculty/admin marks
 * it ready once attendance is confirmed) rather than fully automatic on
 * check-in, so a single no-show doesn't silently generate a certificate.
 */
@RestController
@RequestMapping("/api/registrations/{registrationId}/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
    public ApiResponse<CertificateResponseDTO> generate(@PathVariable Long registrationId,
                                                         @AuthenticationPrincipal User currentUser) {
        Certificate certificate = certificateService.generateForRegistration(registrationId, currentUser);
        return ApiResponse.success("Certificate generated", CertificateResponseDTO.from(certificate));
    }

    @GetMapping
    public ApiResponse<CertificateResponseDTO> get(@PathVariable Long registrationId,
                                                    @AuthenticationPrincipal User currentUser) {
        Certificate certificate = certificateService.findByRegistration(registrationId);

        boolean isOwner = certificate.getRegistration().getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == Role.SUPER_ADMIN
                || currentUser.getRole() == Role.FACULTY_COORDINATOR
                || currentUser.getRole() == Role.STUDENT_ORGANIZER;
        if (!isOwner && !isStaff) {
            throw new AccessDeniedCustomException("You cannot view another student's certificate");
        }

        return ApiResponse.success(CertificateResponseDTO.from(certificate));
    }
}
