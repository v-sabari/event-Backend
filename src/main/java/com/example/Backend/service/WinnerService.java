package com.example.Backend.service;

import com.example.Backend.dto.winner.WinnerRequestDTO;
import com.example.Backend.model.User;
import com.example.Backend.model.Winner;

import java.util.List;

public interface WinnerService {

    Winner addWinner(Long eventId, WinnerRequestDTO dto, User currentUser);

    void removeWinner(Long winnerId, User currentUser);

    List<Winner> findByEvent(Long eventId);
}
