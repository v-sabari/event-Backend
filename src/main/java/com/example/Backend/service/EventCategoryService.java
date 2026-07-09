package com.example.Backend.service;

import com.example.Backend.dto.category.EventCategoryRequestDTO;
import com.example.Backend.model.EventCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventCategoryService {

    EventCategory create(EventCategoryRequestDTO dto);

    EventCategory update(Long id, EventCategoryRequestDTO dto);

    void delete(Long id);

    EventCategory findById(Long id);

    // BE-17: see VenueService.findAll() for rationale.
    Page<EventCategory> findAll(Pageable pageable);
}