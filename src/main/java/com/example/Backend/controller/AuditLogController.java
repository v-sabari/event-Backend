package com.example.Backend.controller;

import com.example.Backend.dto.audit.AuditLogResponseDTO;
import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.model.AuditLog;
import com.example.Backend.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Read-only view over the audit trail. There is deliberately no create/update/
 * delete endpoint here - audit logs are written internally via
 * AuditLogService.record(...) by the services that perform the actions,
 * never directly by a client.
 */
@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<Page<AuditLogResponseDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String entityType
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> result;

        if (performedBy != null && !performedBy.isBlank()) {
            result = auditLogService.findByPerformedBy(performedBy, pageable);
        } else if (entityType != null && !entityType.isBlank()) {
            result = auditLogService.findByEntityType(entityType, pageable);
        } else {
            result = auditLogService.findAll(pageable);
        }

        return ApiResponse.success(result.map(AuditLogResponseDTO::from));
    }
}
