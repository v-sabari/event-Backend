package com.example.Backend.service.impl;

import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.exception.ApiException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.*;
import com.example.Backend.repository.RegistrationRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.EventService;
import com.example.Backend.service.NotificationService;
import com.example.Backend.service.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   EventService eventService,
                                   NotificationService notificationService,
                                   AuditLogService auditLogService) {
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Registration register(Long eventId, User student) {
        Event event = eventService.findById(eventId);

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ApiException("Registrations are only open for published events", HttpStatus.CONFLICT);
        }
        if (Instant.now().isAfter(event.getRegistrationDeadline())) {
            throw new ApiException("The registration deadline for this event has passed", HttpStatus.CONFLICT);
        }
        registrationRepository.findByEventIdAndUserIdAndStatusNot(eventId, student.getId(), RegistrationStatus.CANCELLED)
                .ifPresent(r -> { throw new ApiException("You are already registered for this event", HttpStatus.CONFLICT); });

        long confirmedCount = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.REGISTERED);
        RegistrationStatus status = confirmedCount < event.getMaxParticipants()
                ? RegistrationStatus.REGISTERED
                : RegistrationStatus.WAITLISTED;

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setUser(student);
        registration.setStatus(status);
        registration.setQrToken(UUID.randomUUID().toString().replace("-", ""));

        Registration saved = registrationRepository.save(registration);

        if (status == RegistrationStatus.REGISTERED) {
            notificationService.notify(student,
                    "Registration Confirmed: " + event.getTitle(),
                    "You are registered for '" + event.getTitle() + "'. Your QR entry pass is available in the app.",
                    "REGISTRATION_CONFIRMED", "Event", event.getId(), true);
        } else {
            notificationService.notify(student,
                    "Waitlisted: " + event.getTitle(),
                    "'" + event.getTitle() + "' is full. You have been added to the waiting list and will be notified if a seat opens up.",
                    "REGISTRATION_WAITLISTED", "Event", event.getId(), true);
        }

        log.info("User {} registered for event {} with status {}", student.getRegNumber(), eventId, status);
        auditLogService.record("REGISTRATION_CREATED", "Registration", saved.getId(),
                student.getRegNumber() + " -> event " + eventId + " (" + status + ")");
        return saved;
    }

    @Override
    @Transactional
    public void cancel(Long registrationId, User currentUser) {
        Registration registration = findById(registrationId);

        boolean isOwner = registration.getUser().getId().equals(currentUser.getId());
        boolean isPrivileged = currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.FACULTY_COORDINATOR;
        if (!isOwner && !isPrivileged) {
            throw new AccessDeniedCustomException("You cannot cancel another student's registration");
        }
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new ApiException("Registration is already cancelled", HttpStatus.CONFLICT);
        }

        boolean wasConfirmedSeat = registration.getStatus() == RegistrationStatus.REGISTERED;
        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);

        log.info("Registration {} cancelled by {}", registrationId, currentUser.getRegNumber());
        auditLogService.record("REGISTRATION_CANCELLED", "Registration", registrationId, "Cancelled by " + currentUser.getRegNumber());

        if (wasConfirmedSeat) {
            promoteNextWaitlisted(registration.getEvent());
        }
    }

    /**
     * Object-level authorization for staff acting on a specific event's registrations
     * (roster viewing, QR check-in). A Student Organizer must have created the event
     * themselves; Faculty Coordinator, HOD, and Super Admin act as oversight roles and
     * may act on any event, consistent with EventApprovalServiceImpl's approval override
     * and GalleryServiceImpl.removeImage's ownership-or-privileged-role check.
     */
    private void assertStaffAllowedForEvent(Event event, User currentUser, String message) {
        if (currentUser.getRole() == Role.STUDENT_ORGANIZER) {
            boolean isEventOwner = event.getCreatedBy().getId().equals(currentUser.getId());
            if (!isEventOwner) {
                throw new AccessDeniedCustomException(message);
            }
        }
    }

    private void promoteNextWaitlisted(Event event) {
        List<Registration> waitlist = registrationRepository
                .findByEventIdAndStatusOrderByRegisteredAtAsc(event.getId(), RegistrationStatus.WAITLISTED);
        if (waitlist.isEmpty()) {
            return;
        }
        Registration next = waitlist.get(0);
        next.setStatus(RegistrationStatus.REGISTERED);
        registrationRepository.save(next);

        notificationService.notify(next.getUser(),
                "Seat Available: " + event.getTitle(),
                "A seat opened up for '" + event.getTitle() + "' and you have been moved from the waiting list to confirmed.",
                "WAITLIST_PROMOTED", "Event", event.getId(), true);

        log.info("Promoted waitlisted registration {} to REGISTERED for event {}", next.getId(), event.getId());
        auditLogService.record("WAITLIST_PROMOTED", "Registration", next.getId(), "Promoted " + next.getUser().getRegNumber());
    }

    @Override
    public Registration findById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + id));
    }

    @Override
    public List<Registration> findMine(User student) {
        return registrationRepository.findByUserId(student.getId());
    }

    @Override
    public List<Registration> findByEvent(Long eventId, User currentUser) {
        Event event = eventService.findById(eventId);
        assertStaffAllowedForEvent(event, currentUser, "You can only view the roster for events you created");
        return registrationRepository.findByEventId(eventId);
    }

    @Override
    @Transactional
    public Registration checkInByQrToken(String qrToken, User scannedBy) {
        Registration registration = registrationRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or unrecognized QR code"));

        assertStaffAllowedForEvent(registration.getEvent(), scannedBy,
                "You can only mark attendance for events you created");

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new ApiException("This registration has been cancelled and is not a valid entry pass", HttpStatus.CONFLICT);
        }
        if (registration.getStatus() == RegistrationStatus.WAITLISTED) {
            throw new ApiException("This student is on the waiting list, not a confirmed registrant", HttpStatus.CONFLICT);
        }
        if (registration.getStatus() == RegistrationStatus.ATTENDED) {
            throw new ApiException("This QR pass has already been used to check in", HttpStatus.CONFLICT);
        }

        registration.setStatus(RegistrationStatus.ATTENDED);
        registration.setCheckedInAt(Instant.now());
        registration.setCheckedInBy(scannedBy);
        Registration saved = registrationRepository.save(registration);

        log.info("Registration {} checked in by {}", registration.getId(), scannedBy.getRegNumber());
        auditLogService.record("ATTENDANCE_MARKED", "Registration", registration.getId(),
                "Checked in by " + scannedBy.getRegNumber());
        return saved;
    }
}