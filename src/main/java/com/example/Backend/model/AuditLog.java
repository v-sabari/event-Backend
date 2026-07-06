package com.example.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Immutable record of a significant action taken in the system
 * (who did what, to which entity, when). No update/delete operations
 * are exposed for this entity anywhere in the app - audit logs are
 * append-only by design.
 */
@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** regNumber of the actor, or "SYSTEM"/"ANONYMOUS" when there is no authenticated user. */
    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
