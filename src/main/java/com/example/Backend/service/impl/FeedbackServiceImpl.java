package com.example.Backend.service.impl;

import com.example.Backend.dto.feedback.FeedbackRequestDTO;
import com.example.Backend.exception.ApiException;
import com.example.Backend.model.Event;
import com.example.Backend.model.Feedback;
import com.example.Backend.model.RegistrationStatus;
import com.example.Backend.model.User;
import com.example.Backend.repository.FeedbackRepository;
import com.example.Backend.repository.RegistrationRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.EventService;
import com.example.Backend.service.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    private final FeedbackRepository feedbackRepository;
    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final AuditLogService auditLogService;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository,
                                RegistrationRepository registrationRepository,
                                EventService eventService,
                                AuditLogService auditLogService) {
        this.feedbackRepository = feedbackRepository;
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Feedback submit(Long eventId, FeedbackRequestDTO dto, User student) {
        Event event = eventService.findById(eventId);

        boolean attended = registrationRepository.findByEventIdAndUserIdAndStatusNot(eventId, student.getId(), RegistrationStatus.CANCELLED)
                .filter(r -> r.getStatus() == RegistrationStatus.ATTENDED)
                .isPresent();
        if (!attended) {
            throw new ApiException("Only students who attended the event can leave feedback", HttpStatus.FORBIDDEN);
        }

        if (feedbackRepository.existsByEventIdAndUserId(eventId, student.getId())) {
            throw new ApiException("You have already submitted feedback for this event", HttpStatus.CONFLICT);
        }

        Feedback feedback = new Feedback();
        feedback.setEvent(event);
        feedback.setUser(student);
        feedback.setRating(dto.getRating());
        feedback.setComments(dto.getComments());

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback submitted by {} for event {}: rating={}", student.getRegNumber(), eventId, dto.getRating());
        auditLogService.record("FEEDBACK_SUBMITTED", "Event", eventId, student.getRegNumber() + " rated " + dto.getRating());
        return saved;
    }

    @Override
    public List<Feedback> findByEvent(Long eventId) {
        return feedbackRepository.findByEventId(eventId);
    }

    @Override
    public Double averageRating(Long eventId) {
        return feedbackRepository.findAverageRatingForEvent(eventId);
    }
}
