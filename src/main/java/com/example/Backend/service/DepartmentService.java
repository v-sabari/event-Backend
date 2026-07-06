package com.example.Backend.service;

import com.example.Backend.dto.department.DepartmentRequestDTO;
import com.example.Backend.model.Department;

import java.util.List;

public interface DepartmentService {

    Department create(DepartmentRequestDTO dto);

    Department update(Long id, DepartmentRequestDTO dto);

    void delete(Long id);

    Department findById(Long id);

    List<Department> findAll();
}
