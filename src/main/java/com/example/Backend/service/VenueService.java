package com.example.Backend.service;

import com.example.Backend.dto.venue.VenueRequestDTO;
import com.example.Backend.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VenueService {

    Venue create(VenueRequestDTO dto);

    Venue update(Long id, VenueRequestDTO dto);

    void delete(Long id);

    Venue findById(Long id);

    // BE-17: findAll() returned every venue unbounded; replaced with the
    // same Page<T> pattern already used correctly in NotificationService/
    // AuditLogService. VenueService.findAll() has no other caller besides
    // VenueController, so this is a clean signature swap rather than an
    // additive overload.
    Page<Venue> findAll(Pageable pageable);

    Venue setActive(Long id, boolean active);
}