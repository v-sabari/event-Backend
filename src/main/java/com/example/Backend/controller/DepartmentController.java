package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.department.DepartmentRequestDTO;
import com.example.Backend.dto.department.DepartmentResponseDTO;
import com.example.Backend.model.Department;
import com.example.Backend.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Master data for departments. Read access is open to any authenticated
 * user (needed for populating dropdowns e.g. on the registration form);
 * writes are restricted to SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<DepartmentResponseDTO> create(@Valid @RequestBody DepartmentRequestDTO dto) {
        Department created = departmentService.create(dto);
        return ApiResponse.success("Department created", DepartmentResponseDTO.from(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<DepartmentResponseDTO> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequestDTO dto) {
        Department updated = departmentService.update(id, dto);
        return ApiResponse.success("Department updated", DepartmentResponseDTO.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.message("Department deleted");
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(DepartmentResponseDTO.from(departmentService.findById(id)));
    }

    @GetMapping
    public ApiResponse<Page<DepartmentResponseDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DepartmentResponseDTO> response = departmentService.findAll(PageRequest.of(page, size))
                .map(DepartmentResponseDTO::from);
        return ApiResponse.success(response);
    }
}