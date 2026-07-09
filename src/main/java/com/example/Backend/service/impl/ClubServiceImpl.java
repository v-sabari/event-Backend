package com.example.Backend.service.impl;

import com.example.Backend.dto.club.ClubRequestDTO;
import com.example.Backend.exception.DuplicateResourceException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.Club;
import com.example.Backend.model.Department;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.repository.ClubRepository;
import com.example.Backend.repository.DepartmentRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.ClubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ClubServiceImpl implements ClubService {

    private static final Logger log = LoggerFactory.getLogger(ClubServiceImpl.class);

    private final ClubRepository clubRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ClubServiceImpl(ClubRepository clubRepository,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           AuditLogService auditLogService) {
        this.clubRepository = clubRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Club create(ClubRequestDTO dto) {
        if (clubRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("A club named '" + dto.getName() + "' already exists");
        }

        Club club = new Club();
        applyRequest(club, dto);
        Club saved = clubRepository.save(club);

        log.info("Club created: {}", saved.getName());
        auditLogService.record("CLUB_CREATED", "Club", saved.getId(), "Created club " + saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Club update(Long id, ClubRequestDTO dto) {
        Club club = findById(id);

        clubRepository.findByName(dto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("A club named '" + dto.getName() + "' already exists");
            }
        });

        applyRequest(club, dto);
        Club saved = clubRepository.save(club);

        log.info("Club updated: id={}", id);
        auditLogService.record("CLUB_UPDATED", "Club", id, "Updated club " + saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Club club = findById(id);
        clubRepository.delete(club);
        log.info("Club deleted: id={}", id);
        auditLogService.record("CLUB_DELETED", "Club", id, "Deleted club " + club.getName());
    }

    @Override
    public Club findById(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id: " + id));
    }

    @Override
    public Page<Club> findAll(Pageable pageable) {
        return clubRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Club setActive(Long id, boolean active) {
        Club club = findById(id);
        club.setActive(active);
        Club saved = clubRepository.save(club);
        auditLogService.record(active ? "CLUB_ACTIVATED" : "CLUB_DEACTIVATED", "Club", id, "Club " + club.getName());
        return saved;
    }

    private void applyRequest(Club club, ClubRequestDTO dto) {
        club.setName(dto.getName());
        club.setDescription(dto.getDescription());

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));
            club.setDepartment(department);
        } else {
            club.setDepartment(null);
        }

        if (dto.getCoordinatorId() != null) {
            User coordinator = userRepository.findById(dto.getCoordinatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getCoordinatorId()));
            if (coordinator.getRole() != Role.FACULTY_COORDINATOR) {
                throw new IllegalArgumentException("Assigned coordinator must have the FACULTY_COORDINATOR role");
            }
            club.setCoordinator(coordinator);
        } else {
            club.setCoordinator(null);
        }
    }
}