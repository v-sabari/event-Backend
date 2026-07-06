package com.example.Backend.service;

import com.example.Backend.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    /**
     * Fire-and-forget style record of an action. Reads the current
     * authenticated principal from the SecurityContext itself so every
     * calling service doesn't need to pass the actor around manually.
     */
    void record(String action, String entityType, Long entityId, String details);

    Page<AuditLog> findAll(Pageable pageable);

    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
}
