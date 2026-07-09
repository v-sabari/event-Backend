package com.example.Backend.service.impl;

import com.example.Backend.dto.venue.VenueRequestDTO;
import com.example.Backend.exception.DuplicateResourceException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.Venue;
import com.example.Backend.repository.VenueRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.VenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class VenueServiceImpl implements VenueService {

    private static final Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

    private final VenueRepository venueRepository;
    private final AuditLogService auditLogService;

    public VenueServiceImpl(VenueRepository venueRepository, AuditLogService auditLogService) {
        this.venueRepository = venueRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Venue create(VenueRequestDTO dto) {
        if (venueRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("A venue named '" + dto.getName() + "' already exists");
        }
        Venue venue = new Venue();
        venue.setName(dto.getName());
        venue.setLocation(dto.getLocation());
        venue.setCapacity(dto.getCapacity());

        Venue saved = venueRepository.save(venue);
        log.info("Venue created: {}", saved.getName());
        auditLogService.record("VENUE_CREATED", "Venue", saved.getId(), "Created venue " + saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Venue update(Long id, VenueRequestDTO dto) {
        Venue venue = findById(id);

        venueRepository.findByName(dto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("A venue named '" + dto.getName() + "' already exists");
            }
        });

        venue.setName(dto.getName());
        venue.setLocation(dto.getLocation());
        venue.setCapacity(dto.getCapacity());

        Venue saved = venueRepository.save(venue);
        log.info("Venue updated: id={}", id);
        auditLogService.record("VENUE_UPDATED", "Venue", id, "Updated venue " + saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Venue venue = findById(id);
        venueRepository.delete(venue);
        log.info("Venue deleted: id={}", id);
        auditLogService.record("VENUE_DELETED", "Venue", id, "Deleted venue " + venue.getName());
    }

    @Override
    public Venue findById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
    }

    @Override
    public Page<Venue> findAll(Pageable pageable) {
        return venueRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Venue setActive(Long id, boolean active) {
        Venue venue = findById(id);
        venue.setActive(active);
        Venue saved = venueRepository.save(venue);
        auditLogService.record(active ? "VENUE_ACTIVATED" : "VENUE_DEACTIVATED", "Venue", id, "Venue " + venue.getName());
        return saved;
    }
}