package com.example.Backend.service;

import com.example.Backend.dto.club.ClubRequestDTO;
import com.example.Backend.model.Club;

import java.util.List;

public interface ClubService {

    Club create(ClubRequestDTO dto);

    Club update(Long id, ClubRequestDTO dto);

    void delete(Long id);

    Club findById(Long id);

    List<Club> findAll();

    Club setActive(Long id, boolean active);
}
