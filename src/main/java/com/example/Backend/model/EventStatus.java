package com.example.Backend.model;

/**
 * Workflow: DRAFT -> PENDING_FACULTY -> [PENDING_HOD, optional] -> PENDING_ADMIN
 *           -> PUBLISHED, with REJECTED reachable from any PENDING_* stage
 *           (back to DRAFT for edits) and CANCELLED/COMPLETED as terminal
 *           states reachable after PUBLISHED.
 */
public enum EventStatus {
    DRAFT,
    PENDING_FACULTY_APPROVAL,
    PENDING_HOD_APPROVAL,
    PENDING_ADMIN_APPROVAL,
    REJECTED,
    PUBLISHED,
    CANCELLED,
    COMPLETED
}
