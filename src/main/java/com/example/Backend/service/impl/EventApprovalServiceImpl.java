package com.example.Backend.service.impl;

import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.exception.ApiException;
import com.example.Backend.model.*;
import com.example.Backend.repository.EventApprovalHistoryRepository;
import com.example.Backend.repository.EventRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.EventApprovalService;
import com.example.Backend.service.EventService;
import com.example.Backend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class EventApprovalServiceImpl implements EventApprovalService {

    private static final Logger log = LoggerFactory.getLogger(EventApprovalServiceImpl.class);
    private static final Set<EventStatus> SUBMITTABLE = Set.of(EventStatus.DRAFT, EventStatus.REJECTED);

    private final EventRepository eventRepository;
    private final EventApprovalHistoryRepository historyRepository;
    private final EventService eventService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public EventApprovalServiceImpl(EventRepository eventRepository,
                                     EventApprovalHistoryRepository historyRepository,
                                     EventService eventService,
                                     NotificationService notificationService,
                                     AuditLogService auditLogService) {
        this.eventRepository = eventRepository;
        this.historyRepository = historyRepository;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Event submit(Long eventId, User currentUser) {
        Event event = eventService.findById(eventId);

        boolean isOwner = event.getCreatedBy().getId().equals(currentUser.getId());
        if (!isOwner && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedCustomException("Only the event's creator can submit it for approval");
        }
        if (!SUBMITTABLE.contains(event.getStatus())) {
            throw new ApiException("Event cannot be submitted from status " + event.getStatus(), HttpStatus.CONFLICT);
        }

        EventStatus from = event.getStatus();
        event.setStatus(EventStatus.PENDING_FACULTY_APPROVAL);
        eventRepository.save(event);
        recordHistory(event, from, EventStatus.PENDING_FACULTY_APPROVAL, currentUser, "Submitted for approval");

        log.info("Event submitted for approval: id={} title={}", event.getId(), event.getTitle());
        return event;
    }

    @Override
    @Transactional
    public Event approve(Long eventId, User approver, String remarks) {
        Event event = eventService.findById(eventId);
        EventStatus current = event.getStatus();

        assertApproverAllowedForStage(event, approver, current);

        EventStatus next = switch (current) {
            case PENDING_FACULTY_APPROVAL -> requiresHod(event) ? EventStatus.PENDING_HOD_APPROVAL : EventStatus.PENDING_ADMIN_APPROVAL;
            case PENDING_HOD_APPROVAL -> EventStatus.PENDING_ADMIN_APPROVAL;
            case PENDING_ADMIN_APPROVAL -> EventStatus.PUBLISHED;
            default -> throw new ApiException("Event is not awaiting approval (current status: " + current + ")", HttpStatus.CONFLICT);
        };

        event.setStatus(next);
        eventRepository.save(event);
        recordHistory(event, current, next, approver, remarks);

        if (next == EventStatus.PUBLISHED) {
            notificationService.notify(event.getCreatedBy(),
                    "Event Published: " + event.getTitle(),
                    "Your event '" + event.getTitle() + "' has been approved and published.",
                    "EVENT_APPROVED", "Event", event.getId(), true);
        } else {
            notificationService.notify(event.getCreatedBy(),
                    "Event Progressing: " + event.getTitle(),
                    "Your event '" + event.getTitle() + "' moved to the next approval stage (" + next + ").",
                    "EVENT_STAGE_ADVANCED", "Event", event.getId(), false);
        }

        log.info("Event {} approved by {} ({} -> {})", eventId, approver.getRegNumber(), current, next);
        auditLogService.record("EVENT_APPROVED", "Event", eventId, current + " -> " + next + " by " + approver.getRegNumber());
        return event;
    }

    @Override
    @Transactional
    public Event reject(Long eventId, User approver, String remarks) {
        if (remarks == null || remarks.isBlank()) {
            throw new IllegalArgumentException("Remarks are required when rejecting an event");
        }

        Event event = eventService.findById(eventId);
        EventStatus current = event.getStatus();
        assertApproverAllowedForStage(event, approver, current);

        event.setStatus(EventStatus.REJECTED);
        eventRepository.save(event);
        recordHistory(event, current, EventStatus.REJECTED, approver, remarks);

        notificationService.notify(event.getCreatedBy(),
                "Event Rejected: " + event.getTitle(),
                "Your event '" + event.getTitle() + "' was rejected. Remarks: " + remarks,
                "EVENT_REJECTED", "Event", event.getId(), true);

        log.info("Event {} rejected by {}: {}", eventId, approver.getRegNumber(), remarks);
        auditLogService.record("EVENT_REJECTED", "Event", eventId, "Rejected by " + approver.getRegNumber() + ": " + remarks);
        return event;
    }

    @Override
    public List<EventApprovalHistory> getHistory(Long eventId) {
        eventService.findById(eventId); // 404s if the event doesn't exist
        return historyRepository.findByEventIdOrderByCreatedAtAsc(eventId);
    }

    @Override
    public List<Event> findPendingFor(User approver) {
        return switch (approver.getRole()) {
            case FACULTY_COORDINATOR -> eventRepository.findByStatus(EventStatus.PENDING_FACULTY_APPROVAL);
            case HOD -> eventRepository.findByStatus(EventStatus.PENDING_HOD_APPROVAL);
            case SUPER_ADMIN -> eventRepository.findByStatus(EventStatus.PENDING_ADMIN_APPROVAL);
            default -> List.of();
        };
    }

    // ---- internal helpers ----

    private boolean requiresHod(Event event) {
        return event.getDepartment() != null && event.getDepartment().isHodApprovalRequired();
    }

    private void assertApproverAllowedForStage(Event event, User approver, EventStatus stage) {
        if (approver.getRole() == Role.SUPER_ADMIN) {
            return; // admin can act at any stage as an override
        }
        boolean allowed = switch (stage) {
            case PENDING_FACULTY_APPROVAL -> approver.getRole() == Role.FACULTY_COORDINATOR;
            case PENDING_HOD_APPROVAL -> approver.getRole() == Role.HOD;
            default -> false; // PENDING_ADMIN_APPROVAL and terminal states: SUPER_ADMIN only, handled above
        };
        if (!allowed) {
            throw new AccessDeniedCustomException("You are not authorized to act on this event at its current stage (" + stage + ")");
        }
    }

    private void recordHistory(Event event, EventStatus from, EventStatus to, User actor, String remarks) {
        EventApprovalHistory history = new EventApprovalHistory();
        history.setEvent(event);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setActor(actor);
        history.setRemarks(remarks);
        historyRepository.save(history);
    }
}
