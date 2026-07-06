package com.example.Backend.service.impl;

import com.example.Backend.dto.event.EventRequestDTO;
import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.exception.ApiException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.*;
import com.example.Backend.repository.*;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    /** Statuses an Event can still be edited in - once past the first approval stage, edits should go through resubmission after a reject, not silent mutation. */
    private static final Set<EventStatus> EDITABLE_STATUSES = Set.of(EventStatus.DRAFT, EventStatus.REJECTED);

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final EventCategoryRepository categoryRepository;
    private final ClubRepository clubRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    public EventServiceImpl(EventRepository eventRepository,
                             VenueRepository venueRepository,
                             EventCategoryRepository categoryRepository,
                             ClubRepository clubRepository,
                             DepartmentRepository departmentRepository,
                             AuditLogService auditLogService) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.categoryRepository = categoryRepository;
        this.clubRepository = clubRepository;
        this.departmentRepository = departmentRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Event createDraft(EventRequestDTO dto, User creator) {
        validateTimes(dto);
        assertVenueAvailable(dto.getVenueId(), dto.getStartTime(), dto.getEndTime(), null);

        Event event = new Event();
        event.setCreatedBy(creator);
        event.setStatus(EventStatus.DRAFT);
        applyRequest(event, dto);

        Event saved = eventRepository.save(event);
        log.info("Event draft created: '{}' by {}", saved.getTitle(), creator.getRegNumber());
        auditLogService.record("EVENT_DRAFT_CREATED", "Event", saved.getId(), "Created draft '" + saved.getTitle() + "'");
        return saved;
    }

    @Override
    @Transactional
    public Event updateDraft(Long id, EventRequestDTO dto, User currentUser) {
        Event event = findById(id);
        assertOwnerOrAdmin(event, currentUser);

        if (!EDITABLE_STATUSES.contains(event.getStatus())) {
            throw new ApiException("Only draft or rejected events can be edited", HttpStatus.CONFLICT);
        }

        validateTimes(dto);
        assertVenueAvailable(dto.getVenueId(), dto.getStartTime(), dto.getEndTime(), id);

        applyRequest(event, dto);
        // Editing a rejected event resets it back to draft so it must be resubmitted deliberately.
        if (event.getStatus() == EventStatus.REJECTED) {
            event.setStatus(EventStatus.DRAFT);
        }

        Event saved = eventRepository.save(event);
        log.info("Event draft updated: id={} by {}", id, currentUser.getRegNumber());
        auditLogService.record("EVENT_DRAFT_UPDATED", "Event", id, "Updated draft '" + saved.getTitle() + "'");
        return saved;
    }

    @Override
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    @Override
    public List<Event> findVisibleTo(User currentUser) {
        return switch (currentUser.getRole()) {
            case SUPER_ADMIN -> eventRepository.findAll();
            case FACULTY_COORDINATOR, HOD -> currentUser.getDepartment() != null
                    ? eventRepository.findByDepartmentId(currentUser.getDepartment().getId())
                    : List.of();
            case STUDENT_ORGANIZER -> eventRepository.findByCreatedById(currentUser.getId());
            case STUDENT -> findPublished();
        };
    }

    @Override
    public List<Event> findPublished() {
        return eventRepository.findByStatusIn(List.of(EventStatus.PUBLISHED, EventStatus.COMPLETED));
    }

    @Override
    @Transactional
    public void cancel(Long id, User currentUser) {
        Event event = findById(id);
        assertOwnerOrAdmin(event, currentUser);

        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
            throw new ApiException("Event is already " + event.getStatus().name().toLowerCase(), HttpStatus.CONFLICT);
        }

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
        log.info("Event cancelled: id={} by {}", id, currentUser.getRegNumber());
        auditLogService.record("EVENT_CANCELLED", "Event", id, "Cancelled '" + event.getTitle() + "'");
    }

    @Override
    @Transactional
    public void markCompleted(Long id, User currentUser) {
        Event event = findById(id);
        assertOwnerOrAdmin(event, currentUser);
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ApiException("Only a published event can be marked completed", HttpStatus.CONFLICT);
        }
        event.setStatus(EventStatus.COMPLETED);
        eventRepository.save(event);
        log.info("Event marked completed: id={} by {}", id, currentUser.getRegNumber());
        auditLogService.record("EVENT_COMPLETED", "Event", id, "Marked '" + event.getTitle() + "' completed");
    }

    @Override
    public void assertVenueAvailable(Long venueId, Instant startTime, Instant endTime, Long excludeEventId) {
        List<Event> overlaps = eventRepository.findOverlappingBookings(venueId, startTime, endTime, excludeEventId);
        if (!overlaps.isEmpty()) {
            throw new ApiException(
                    "Venue is already booked for an overlapping time slot (conflicts with '" + overlaps.get(0).getTitle() + "')",
                    HttpStatus.CONFLICT
            );
        }
    }

    @Override
    public List<Event> search(String name, Long departmentId, Long categoryId, Long venueId, java.time.LocalDate date) {
        return eventRepository.search(
                (name != null && !name.isBlank()) ? name.trim() : null,
                departmentId, categoryId, venueId, date
        );
    }

    private void validateTimes(EventRequestDTO dto) {
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (dto.getRegistrationDeadline().isAfter(dto.getStartTime())) {
            throw new IllegalArgumentException("Registration deadline must be before the event starts");
        }
    }

    private void assertOwnerOrAdmin(Event event, User currentUser) {
        boolean isOwner = event.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.SUPER_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("You do not have permission to modify this event");
        }
    }

    private void applyRequest(Event event, EventRequestDTO dto) {
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setRegistrationDeadline(dto.getRegistrationDeadline());
        event.setMaxParticipants(dto.getMaxParticipants());
        event.setFee(dto.getFee());
        event.setBannerUrl(dto.getBannerUrl());

        Venue venue = venueRepository.findById(dto.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + dto.getVenueId()));
        event.setVenue(venue);

        if (dto.getCategoryId() != null) {
            event.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId())));
        } else {
            event.setCategory(null);
        }

        if (dto.getClubId() != null) {
            event.setClub(clubRepository.findById(dto.getClubId())
                    .orElseThrow(() -> new ResourceNotFoundException("Club not found with id: " + dto.getClubId())));
        } else {
            event.setClub(null);
        }

        if (dto.getDepartmentId() != null) {
            event.setDepartment(departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId())));
        } else {
            event.setDepartment(null);
        }
    }
}
