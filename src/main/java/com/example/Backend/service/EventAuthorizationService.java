package com.example.Backend.service;

import com.example.Backend.model.Event;
import com.example.Backend.model.User;

/**
 * Single, reusable authorization check for the "current user may act on this
 * specific event" pattern needed by every event-scoped staff action: roster
 * viewing, QR check-in, certificate generation, winner management, and any
 * future module built the same way.
 * <p>
 * BE-02: previously each service (Winner, Certificate) implemented - or
 * simply forgot to implement - this check independently, and
 * RegistrationServiceImpl carried its own private copy of the same logic.
 * That duplication is exactly how the check gets forgotten in a new module
 * (as it was in WinnerServiceImpl and CertificateServiceImpl - see BE-01).
 * This interface is the single place the rule now lives.
 * <p>
 * Rule: a Student Organizer may only act on events they created themselves;
 * Faculty Coordinator, HOD, and Super Admin are oversight roles and may act
 * on any event, matching the scope already granted to those roles by the
 * relevant {@code @PreAuthorize} annotations at the controller layer.
 * <p>
 * This intentionally does NOT cover ownership of a <i>different</i> resource
 * type - e.g. "did this user upload this gallery image" ({@code
 * GalleryServiceImpl.removeImage}) or "is this the student's own
 * registration" ({@code RegistrationServiceImpl.cancel}). Those are distinct
 * actor/resource relationships with their own rules and stay in their own
 * services.
 */
public interface EventAuthorizationService {

    /** True if currentUser may act on this event as event-scoped staff. */
    boolean canActOnEvent(Event event, User currentUser);

    /** Throws {@code AccessDeniedCustomException(message)} if canActOnEvent(...) is false. */
    void assertCanActOnEvent(Event event, User currentUser, String message);
}