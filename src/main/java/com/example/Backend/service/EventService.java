package com.example.Backend.service;

import com.example.Backend.dto.event.EventRequestDTO;
import com.example.Backend.model.Event;
import com.example.Backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {

    Event createDraft(EventRequestDTO dto, User creator);

    Event updateDraft(Long id, EventRequestDTO dto, User currentUser);

    Event findById(Long id);

    // BE-17: EventController.myVisibleEvents() returned every visible event
    // unbounded - for SUPER_ADMIN in particular this was eventRepository.findAll()
    // with no limit at all. The unpaged findVisibleTo(User) below had no other
    // caller besides that one controller method, so it is removed rather than
    // kept as unused dead code; this Pageable overload replaces it entirely,
    // matching the Page<T> pattern already used correctly in
    // NotificationService/AuditLogService.
    Page<Event> findVisibleTo(User currentUser, Pageable pageable);

    /** Public/student-facing calendar & listing - published events only (Event Calendar module). */
    List<Event> findPublished();

    void cancel(Long id, User currentUser);

    void markCompleted(Long id, User currentUser);

    /** Venue Double Booking Prevention - throws if the venue is already booked in this window. */
    void assertVenueAvailable(Long venueId, java.time.Instant startTime, java.time.Instant endTime, Long excludeEventId);

    /** Search & Filters module. */
    List<Event> search(String name, Long departmentId, Long categoryId, Long venueId, java.time.LocalDate date);
}