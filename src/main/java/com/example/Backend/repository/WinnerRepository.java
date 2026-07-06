package com.example.Backend.repository;

import com.example.Backend.model.Winner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WinnerRepository extends JpaRepository<Winner, Long> {

    List<Winner> findByEventId(Long eventId);
}
