package com.example.Backend.service.impl;

import com.example.Backend.dto.category.EventCategoryRequestDTO;
import com.example.Backend.exception.DuplicateResourceException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.EventCategory;
import com.example.Backend.repository.EventCategoryRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.EventCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventCategoryServiceImpl implements EventCategoryService {

    private static final Logger log = LoggerFactory.getLogger(EventCategoryServiceImpl.class);

    private final EventCategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    public EventCategoryServiceImpl(EventCategoryRepository categoryRepository, AuditLogService auditLogService) {
        this.categoryRepository = categoryRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public EventCategory create(EventCategoryRequestDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("A category named '" + dto.getName() + "' already exists");
        }
        EventCategory category = new EventCategory();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        EventCategory saved = categoryRepository.save(category);
        log.info("Event category created: {}", saved.getName());
        auditLogService.record("EVENT_CATEGORY_CREATED", "EventCategory", saved.getId(), "Created category " + saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public EventCategory update(Long id, EventCategoryRequestDTO dto) {
        EventCategory category = findById(id);

        categoryRepository.findByName(dto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("A category named '" + dto.getName() + "' already exists");
            }
        });

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        EventCategory saved = categoryRepository.save(category);
        log.info("Event category updated: id={}", id);
        auditLogService.record("EVENT_CATEGORY_UPDATED", "EventCategory", id, "Updated category " + saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EventCategory category = findById(id);
        categoryRepository.delete(category);
        log.info("Event category deleted: id={}", id);
        auditLogService.record("EVENT_CATEGORY_DELETED", "EventCategory", id, "Deleted category " + category.getName());
    }

    @Override
    public EventCategory findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event category not found with id: " + id));
    }

    @Override
    public List<EventCategory> findAll() {
        return categoryRepository.findAll();
    }
}
