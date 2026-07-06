package com.example.Backend.repository;

import com.example.Backend.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByName(String name);

    boolean existsByName(String name);

    List<Club> findByDepartmentId(Long departmentId);

    List<Club> findByCoordinatorId(Long coordinatorId);

    List<Club> findByActiveTrue();
}
