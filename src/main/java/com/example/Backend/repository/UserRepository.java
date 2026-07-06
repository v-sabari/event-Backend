package com.example.Backend.repository;

import com.example.Backend.model.Role;
import com.example.Backend.model.User;
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

    List<User> findByRole(Role role);

    List<User> findByDepartmentId(Long departmentId);
}