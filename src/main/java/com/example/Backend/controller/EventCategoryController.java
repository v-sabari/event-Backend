package com.example.Backend.controller;

import com.example.Backend.dto.category.EventCategoryRequestDTO;
import com.example.Backend.dto.category.EventCategoryResponseDTO;
import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.model.EventCategory;
import com.example.Backend.service.EventCategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Master data used to categorize events (e.g. "Technical", "Cultural",
 * "Sports"). Kept as its own module rather than a free-text field on Event
 * so the future Event Management module gets consistent, filterable categories.
 */
@RestController
@RequestMapping("/api/event-categories")
public class EventCategoryController {

    private final EventCategoryService categoryService;

    public EventCategoryController(EventCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<EventCategoryResponseDTO> create(@Valid @RequestBody EventCategoryRequestDTO dto) {
        EventCategory created = categoryService.create(dto);
        return ApiResponse.success("Category created", EventCategoryResponseDTO.from(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR')")
    public ApiResponse<EventCategoryResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EventCategoryRequestDTO dto) {
        EventCategory updated = categoryService.update(id, dto);
        return ApiResponse.success("Category updated", EventCategoryResponseDTO.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.message("Category deleted");
    }

    @GetMapping("/{id}")
    public ApiResponse<EventCategoryResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(EventCategoryResponseDTO.from(categoryService.findById(id)));
    }

    @GetMapping
    public ApiResponse<Page<EventCategoryResponseDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<EventCategoryResponseDTO> response = categoryService.findAll(PageRequest.of(page, size))
                .map(EventCategoryResponseDTO::from);
        return ApiResponse.success(response);
    }
}