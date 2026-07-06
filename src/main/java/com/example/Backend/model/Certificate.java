package com.example.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "certificates")
@Data
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false, unique = true)
    private Registration registration;

    /** Stored filename on disk (via the existing FileStorageService/UploadedFile mechanism). */
    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "certificate_code", nullable = false, unique = true, length = 40)
    private String certificateCode;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;

    @PrePersist
    protected void onCreate() {
        this.generatedAt = Instant.now();
    }
}
