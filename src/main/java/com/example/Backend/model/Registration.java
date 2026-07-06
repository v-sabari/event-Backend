package com.example.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "registrations")
@Data
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status;

    /**
     * Opaque random token embedded in the QR entry pass. Deliberately not the
     * registration's numeric id (which would be guessable/enumerable) -
     * this is the value the QR image encodes and the scan-verify endpoint looks up.
     */
    @Column(name = "qr_token", nullable = false, unique = true, length = 64)
    private String qrToken;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checked_in_by")
    private User checkedInBy;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = Instant.now();
    }
}
