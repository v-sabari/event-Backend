package com.example.Backend.model;

/**
 * Fixed set of roles for the platform. Kept as a Java enum (not a lookup
 * table) because the set of roles is small, stable, and referenced directly
 * in security rules (@PreAuthorize) — adding a role is a deliberate code
 * change, not runtime configuration.
 */
public enum Role {
    SUPER_ADMIN,
    FACULTY_COORDINATOR,
    HOD,
    STUDENT_ORGANIZER,
    STUDENT
}
