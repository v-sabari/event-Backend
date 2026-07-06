package com.example.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Metadata record for every file stored on disk. This is a *foundation* -
 * it doesn't yet link to an Event (banner image) or anything else, since
 * Event Management isn't implemented yet, but future modules can add a
 * nullable FK (e.g. event_id) without touching this table's core shape.
 */
@Entity
@Table(name = "uploaded_files")
@Data
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    /** Randomized name actually used on disk, to avoid collisions/path traversal. */
    @Column(name = "stored_file_name", nullable = false, unique = true, length = 255)
    private String storedFileName;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
