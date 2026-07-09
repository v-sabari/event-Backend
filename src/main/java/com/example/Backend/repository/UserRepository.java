package com.example.Backend.repository;

import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByRegNumber(String regNumber);

    Optional<User> findByEmail(String email);

    boolean existsByRegNumber(String regNumber);

    boolean existsByEmail(String email);

    // BE-17: UserController.list() returned every matching user unbounded -
    // findAll(Pageable) is inherited for free from JpaRepository; this
    // Pageable overload of findByRole is the one addition needed. The old
    // unpaged findByRole(Role) had no caller besides UserServiceImpl.findByRole,
    // which itself had no caller besides UserController, so it was a clean
    // signature swap rather than an additive overload (same reasoning as
    // VenueService.findAll() etc.).
    Page<User> findByRole(Role role, Pageable pageable);

    List<User> findByDepartmentId(Long departmentId);
}