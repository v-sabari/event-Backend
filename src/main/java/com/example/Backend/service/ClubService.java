package com.example.Backend.service;

import com.example.Backend.dto.club.ClubRequestDTO;
import com.example.Backend.model.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClubService {

    Club create(ClubRequestDTO dto);

    Club update(Long id, ClubRequestDTO dto);

    void delete(Long id);

    Club findById(Long id);

    // BE-17: see VenueService.findAll() for rationale.
    Page<Club> findAll(Pageable pageable);

    Club setActive(Long id, boolean active);
}