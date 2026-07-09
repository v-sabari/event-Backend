package com.example.Backend.service.impl;

import com.example.Backend.dto.auth.RegisterRequestDTO;
import com.example.Backend.exception.DuplicateResourceException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.Department;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.repository.DepartmentRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserServiceImpl(UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder,
                           AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public User register(RegisterRequestDTO dto) {
        if (userRepository.existsByRegNumber(dto.getRegNumber())) {
            throw new DuplicateResourceException("A user with reg number '" + dto.getRegNumber() + "' already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + dto.getEmail() + "' already exists");
        }

        Role role;
        try {
            role = Role.valueOf(dto.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + dto.getRole());
        }

        User user = new User();
        user.setRegNumber(dto.getRegNumber());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setEnabled(true);

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));
            user.setDepartment(department);
        }

        User saved = userRepository.save(user);
        log.info("Registered new user regNumber={} role={}", saved.getRegNumber(), saved.getRole());
        auditLogService.record("USER_REGISTERED", "User", saved.getId(),
                "Registered user " + saved.getRegNumber() + " with role " + saved.getRole());
        return saved;
    }

    @Override
    public User findByRegNumber(String regNumber) {
        return userRepository.findByRegNumber(regNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with reg number: " + regNumber));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> findByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    @Override
    @Transactional
    public User setEnabled(Long id, boolean enabled) {
        User user = findById(id);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        log.info("User id={} enabled set to {}", id, enabled);
        auditLogService.record(enabled ? "USER_ENABLED" : "USER_DISABLED", "User", id,
                "User " + user.getRegNumber() + (enabled ? " enabled" : " disabled"));
        return saved;
    }

    @Override
    @Transactional
    public User changeRole(Long id, Role role) {
        User user = findById(id);
        Role oldRole = user.getRole();
        user.setRole(role);
        User saved = userRepository.save(user);
        log.info("User id={} role changed {} -> {}", id, oldRole, role);
        auditLogService.record("USER_ROLE_CHANGED", "User", id,
                "Role changed from " + oldRole + " to " + role + " for user " + user.getRegNumber());
        return saved;
    }
}