package com.example.Backend.service;

import com.example.Backend.dto.category.EventCategoryRequestDTO;
import com.example.Backend.model.EventCategory;

import java.util.List;

public interface EventCategoryService {

    EventCategory create(EventCategoryRequestDTO dto);

    EventCategory update(Long id, EventCategoryRequestDTO dto);

    void delete(Long id);

    EventCategory findById(Long id);

    List<EventCategory> findAll();
}
