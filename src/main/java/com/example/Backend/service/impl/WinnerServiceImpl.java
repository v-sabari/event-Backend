package com.example.Backend.service.impl;

import com.example.Backend.dto.winner.WinnerRequestDTO;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.Event;
import com.example.Backend.model.User;
import com.example.Backend.model.Winner;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.WinnerRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.EventAuthorizationService;
import com.example.Backend.service.EventService;
import com.example.Backend.service.NotificationService;
import com.example.Backend.service.WinnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WinnerServiceImpl implements WinnerService {

    private static final Logger log = LoggerFactory.getLogger(WinnerServiceImpl.class);

    private final WinnerRepository winnerRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final EventAuthorizationService eventAuthorizationService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public WinnerServiceImpl(WinnerRepository winnerRepository,
                             UserRepository userRepository,
                             EventService eventService,
                             EventAuthorizationService eventAuthorizationService,
                             NotificationService notificationService,
                             AuditLogService auditLogService) {
        this.winnerRepository = winnerRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
        this.eventAuthorizationService = eventAuthorizationService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Winner addWinner(Long eventId, WinnerRequestDTO dto, User currentUser) {
        Event event = eventService.findById(eventId);
        eventAuthorizationService.assertCanActOnEvent(event, currentUser,
                "You can only add winners for events you created");

        Winner winner = new Winner();
        winner.setEvent(event);
        winner.setParticipantName(dto.getParticipantName());
        winner.setPosition(dto.getPosition());
        winner.setPrize(dto.getPrize());

        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
            winner.setUser(user);
        }

        Winner saved = winnerRepository.save(winner);

        if (winner.getUser() != null) {
            notificationService.notify(winner.getUser(),
                    "Congratulations! " + event.getTitle(),
                    "You have been recorded as " + dto.getPosition() + " in '" + event.getTitle() + "'.",
                    "WINNER_ANNOUNCED", "Event", event.getId(), true);
        }

        log.info("Winner added to event {}: {} ({})", eventId, dto.getParticipantName(), dto.getPosition());
        auditLogService.record("WINNER_ADDED", "Event", eventId, dto.getParticipantName() + " - " + dto.getPosition());
        return saved;
    }

    @Override
    @Transactional
    public void removeWinner(Long winnerId, User currentUser) {
        Winner winner = winnerRepository.findById(winnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Winner record not found with id: " + winnerId));
        eventAuthorizationService.assertCanActOnEvent(winner.getEvent(), currentUser,
                "You can only remove winners for events you created");

        winnerRepository.delete(winner);
        log.info("Winner {} removed by {}", winnerId, currentUser.getRegNumber());
        auditLogService.record("WINNER_REMOVED", "Winner", winnerId, "Removed by " + currentUser.getRegNumber());
    }

    @Override
    public List<Winner> findByEvent(Long eventId) {
        return winnerRepository.findByEventId(eventId);
    }
}