package com.example.Backend.service;

import com.example.Backend.model.Registration;
import com.example.Backend.model.User;

import java.util.List;

public interface RegistrationService {

    /** Registers a student, enforcing deadline + capacity; overflows onto the waiting list. */
    Registration register(Long eventId, User student);

    /** Cancels the student's own registration; if it was a confirmed seat, promotes the next waitlisted student. */
    void cancel(Long registrationId, User currentUser);

    Registration findById(Long id);

    List<Registration> findMine(User student);

    /** Roster for an event - organizer/faculty/admin only (enforced at controller level). */
    List<Registration> findByEvent(Long eventId);

    /** QR Attendance: looks up the registration by its QR token and marks attendance, once. */
    Registration checkInByQrToken(String qrToken, User scannedBy);
}
