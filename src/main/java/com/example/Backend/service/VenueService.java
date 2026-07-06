package com.example.Backend.service;

import com.example.Backend.dto.venue.VenueRequestDTO;
import com.example.Backend.model.Venue;

import java.util.List;

public interface VenueService {

    Venue create(VenueRequestDTO dto);

    Venue update(Long id, VenueRequestDTO dto);

    void delete(Long id);

    Venue findById(Long id);

    List<Venue> findAll();

    Venue setActive(Long id, boolean active);
}
