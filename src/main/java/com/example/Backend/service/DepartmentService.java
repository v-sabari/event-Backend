package com.example.Backend.service;

import com.example.Backend.dto.department.DepartmentRequestDTO;
import com.example.Backend.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {

    Department create(DepartmentRequestDTO dto);

    Department update(Long id, DepartmentRequestDTO dto);

    void delete(Long id);

    Department findById(Long id);

    // BE-17: see VenueService.findAll() for rationale.
    Page<Department> findAll(Pageable pageable);
}