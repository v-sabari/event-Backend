package com.example.Backend.service.impl;

import com.example.Backend.dto.event.EventResponseDTO;
import com.example.Backend.dto.registration.RegistrationResponseDTO;
import com.example.Backend.model.Event;
import com.example.Backend.model.EventStatus;
import com.example.Backend.model.RegistrationStatus;
import com.example.Backend.model.User;
import com.example.Backend.repository.*;
import com.example.Backend.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Pure aggregation layer over existing repositories - deliberately does not
 * introduce new tables or duplicate query logic already owned by
 * EventRepository/RegistrationRepository/etc.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final CertificateRepository certificateRepository;
    private final NotificationRepository notificationRepository;
    private final FeedbackRepository feedbackRepository;

    public DashboardServiceImpl(UserRepository userRepository,
                                 EventRepository eventRepository,
                                 RegistrationRepository registrationRepository,
                                 CertificateRepository certificateRepository,
                                 NotificationRepository notificationRepository,
                                 FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.certificateRepository = certificateRepository;
        this.notificationRepository = notificationRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public Map<String, Object> adminSummary() {
        long pendingApprovals = eventRepository.countByStatus(EventStatus.PENDING_FACULTY_APPROVAL)
                + eventRepository.countByStatus(EventStatus.PENDING_HOD_APPROVAL)
                + eventRepository.countByStatus(EventStatus.PENDING_ADMIN_APPROVAL);

        List<RegistrationResponseDTO> recentRegistrations = registrationRepository.findTop10ByOrderByRegisteredAtDesc()
                .stream().map(RegistrationResponseDTO::from).toList();

        return Map.of(
                "totalUsers", userRepository.count(),
                "totalEvents", eventRepository.count(),
                "pendingApprovals", pendingApprovals,
                "activeEvents", eventRepository.countByStatus(EventStatus.PUBLISHED),
                "todaysEvents", eventRepository.countTodayEvents(),
                "recentRegistrations", recentRegistrations
        );
    }

    @Override
    public Map<String, Object> organizerSummary(User organizer) {
        List<Event> myEvents = eventRepository.findByCreatedById(organizer.getId());

        long pendingApproval = myEvents.stream().filter(e ->
                e.getStatus() == EventStatus.PENDING_FACULTY_APPROVAL
                        || e.getStatus() == EventStatus.PENDING_HOD_APPROVAL
                        || e.getStatus() == EventStatus.PENDING_ADMIN_APPROVAL
        ).count();

        long totalParticipants = myEvents.stream()
                .mapToLong(e -> registrationRepository.countByEventIdAndStatus(e.getId(), RegistrationStatus.REGISTERED)
                        + registrationRepository.countByEventIdAndStatus(e.getId(), RegistrationStatus.ATTENDED))
                .sum();

        double avgFeedback = myEvents.stream()
                .map(e -> feedbackRepository.findAverageRatingForEvent(e.getId()))
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return Map.of(
                "myEvents", myEvents.stream().map(EventResponseDTO::from).toList(),
                "pendingApproval", pendingApproval,
                "totalParticipants", totalParticipants,
                "averageFeedbackRating", avgFeedback
        );
    }

    @Override
    public Map<String, Object> studentSummary(User student) {
        List<Event> upcoming = eventRepository.findByStatusIn(List.of(EventStatus.PUBLISHED)).stream()
                .filter(e -> e.getStartTime().isAfter(Instant.now()))
                .toList();

        List<RegistrationResponseDTO> registered = registrationRepository.findByUserId(student.getId()).stream()
                .filter(r -> r.getStatus() != RegistrationStatus.CANCELLED)
                .map(RegistrationResponseDTO::from)
                .toList();

        long certificateCount = certificateRepository.findByRegistrationUserId(student.getId()).size();
        long unreadNotifications = notificationRepository.countByUserIdAndReadFalse(student.getId());

        return Map.of(
                "upcomingEvents", upcoming.stream().map(EventResponseDTO::from).toList(),
                "registeredEvents", registered,
                "certificateCount", certificateCount,
                "unreadNotifications", unreadNotifications
        );
    }
}
