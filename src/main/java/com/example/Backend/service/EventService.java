package com.example.Backend.service;

import com.example.Backend.dto.event.EventRequestDTO;
import com.example.Backend.model.Event;
import com.example.Backend.model.User;

import java.util.List;

public interface EventService {

    Event createDraft(EventRequestDTO dto, User creator);

    Event updateDraft(Long id, EventRequestDTO dto, User currentUser);

    Event findById(Long id);

    /** Role-aware listing: organizers see their own, faculty/HOD see their department's, admin sees all. */
    List<Event> findVisibleTo(User currentUser);

    /** Public/student-facing calendar & listing - published events only (Event Calendar module). */
    List<Event> findPublished();

    void cancel(Long id, User currentUser);

    void markCompleted(Long id, User currentUser);

    /** Venue Double Booking Prevention - throws if the venue is already booked in this window. */
    void assertVenueAvailable(Long venueId, java.time.Instant startTime, java.time.Instant endTime, Long excludeEventId);

    /** Search & Filters module. */
    List<Event> search(String name, Long departmentId, Long categoryId, Long venueId, java.time.LocalDate date);
}
