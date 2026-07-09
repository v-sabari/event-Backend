package com.example.Backend.service.impl;

import com.example.Backend.dto.department.DepartmentRequestDTO;
import com.example.Backend.exception.DuplicateResourceException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.Department;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.repository.DepartmentRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 UserRepository userRepository,
                                 AuditLogService auditLogService) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Department create(DepartmentRequestDTO dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("A department named '" + dto.getName() + "' already exists");
        }
        if (departmentRepository.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("A department with code '" + dto.getCode() + "' already exists");
        }

        Department department = new Department();
        department.setName(dto.getName());
        department.setCode(dto.getCode().toUpperCase());
        department.setDescription(dto.getDescription());
        department.setHodApprovalRequired(dto.isHodApprovalRequired());
        applyHod(department, dto.getHodId());

        Department saved = departmentRepository.save(department);
        log.info("Department created: {} ({})", saved.getName(), saved.getCode());
        auditLogService.record("DEPARTMENT_CREATED", "Department", saved.getId(), "Created department " + saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Department update(Long id, DepartmentRequestDTO dto) {
        Department department = findById(id);

        departmentRepository.findByName(dto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("A department named '" + dto.getName() + "' already exists");
            }
        });
        departmentRepository.findByCode(dto.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("A department with code '" + dto.getCode() + "' already exists");
            }
        });

        department.setName(dto.getName());
        department.setCode(dto.getCode().toUpperCase());
        department.setDescription(dto.getDescription());
        department.setHodApprovalRequired(dto.isHodApprovalRequired());
        applyHod(department, dto.getHodId());

        Department saved = departmentRepository.save(department);
        log.info("Department updated: id={}", id);
        auditLogService.record("DEPARTMENT_UPDATED", "Department", id, "Updated department " + saved.getName());
        return saved;
    }

    private void applyHod(Department department, Long hodId) {
        if (hodId == null) {
            department.setHod(null);
            return;
        }
        User hod = userRepository.findById(hodId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + hodId));
        if (hod.getRole() != Role.HOD) {
            throw new IllegalArgumentException("Assigned HOD must have the HOD role");
        }
        department.setHod(hod);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = findById(id);
        departmentRepository.delete(department);
        log.info("Department deleted: id={}", id);
        auditLogService.record("DEPARTMENT_DELETED", "Department", id, "Deleted department " + department.getName());
    }

    @Override
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    public Page<Department> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }
}