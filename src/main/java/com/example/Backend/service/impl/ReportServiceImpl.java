package com.example.Backend.service.impl;

import com.example.Backend.model.*;
import com.example.Backend.repository.*;
import com.example.Backend.service.ReportService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * All reports are computed from existing repositories via in-memory
 * grouping - deliberately avoids introducing a parallel reporting schema
 * or duplicating query logic that EventRepository/RegistrationRepository/
 * FeedbackRepository already own. Fine at college-event-system scale;
 * would move to DB-side aggregation queries if data volume grew significantly.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(EventRepository eventRepository,
                              RegistrationRepository registrationRepository,
                              FeedbackRepository feedbackRepository,
                              UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Map<String, Object>> departmentWiseEvents() {
        List<Event> events = eventRepository.findAll();
        Map<String, Long> counts = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDepartment() != null ? e.getDepartment().getName() : "Unassigned",
                        Collectors.counting()));
        return toSortedList(counts, "department", "eventCount");
    }

    @Override
    public List<Map<String, Object>> categoryWiseEvents() {
        List<Event> events = eventRepository.findAll();
        Map<String, Long> counts = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() != null ? e.getCategory().getName() : "Uncategorized",
                        Collectors.counting()));
        return toSortedList(counts, "category", "eventCount");
    }

    @Override
    public List<Map<String, Object>> attendanceReport() {
        List<Event> events = eventRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Event e : events) {
            long registeredOrAttended = registrationRepository.countByEventIdAndStatus(e.getId(), RegistrationStatus.REGISTERED)
                    + registrationRepository.countByEventIdAndStatus(e.getId(), RegistrationStatus.ATTENDED);
            long attended = registrationRepository.countByEventIdAndStatus(e.getId(), RegistrationStatus.ATTENDED);
            double rate = registeredOrAttended > 0 ? (attended * 100.0 / registeredOrAttended) : 0.0;

            report.add(Map.of(
                    "eventId", e.getId(),
                    "eventTitle", e.getTitle(),
                    "registered", registeredOrAttended,
                    "attended", attended,
                    "attendanceRatePercent", Math.round(rate * 100.0) / 100.0
            ));
        }
        return report;
    }

    @Override
    public List<Map<String, Object>> registrationReport() {
        List<Event> events = eventRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Event e : events) {
            report.add(Map.of(
                    "eventId", e.getId(),
                    "eventTitle", e.getTitle(),
                    "registered", registrationRepository.countByEventIdAndStatus(e.getId(), RegistrationStatus.REGISTERED),
                    "waitlisted", registrationRepository.countByEventIdAndStatus(e.getId(), RegistrationStatus.WAITLISTED),
                    "cancelled", registrationRepository.countByEventIdAndStatus(e.getId(), RegistrationStatus.CANCELLED),
                    "attended", registrationRepository.countByEventIdAndStatus(e.getId(), RegistrationStatus.ATTENDED)
            ));
        }
        return report;
    }

    @Override
    public Map<String, Object> feedbackAnalysis() {
        List<Event> events = eventRepository.findAll();
        List<Map<String, Object>> perEvent = new ArrayList<>();
        List<Double> allAverages = new ArrayList<>();

        for (Event e : events) {
            List<Feedback> feedbacks = feedbackRepository.findByEventId(e.getId());
            if (feedbacks.isEmpty()) continue;

            double avg = feedbacks.stream().mapToInt(Feedback::getRating).average().orElse(0.0);
            allAverages.add(avg);
            perEvent.add(Map.of(
                    "eventId", e.getId(),
                    "eventTitle", e.getTitle(),
                    "averageRating", Math.round(avg * 100.0) / 100.0,
                    "responseCount", feedbacks.size()
            ));
        }

        double overallAverage = allAverages.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("perEvent", perEvent);
        result.put("overallAverageRating", Math.round(overallAverage * 100.0) / 100.0);
        return result;
    }

    @Override
    public Map<String, Object> mostActiveDepartment() {
        List<Event> events = eventRepository.findByDepartmentIdIsNotNull();
        Map<String, Long> counts = events.stream()
                .collect(Collectors.groupingBy(e -> e.getDepartment().getName(), Collectors.counting()));

        Optional<Map.Entry<String, Long>> top = counts.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        Map<String, Object> result = new HashMap<>();
        result.put("department", top.map(Map.Entry::getKey).orElse("N/A"));
        result.put("eventCount", top.map(Map.Entry::getValue).orElse(0L));
        return result;
    }

    @Override
    public Map<String, Object> mostActiveStudent() {
        List<Registration> registrations = registrationRepository.findAll().stream()
                .filter(r -> r.getStatus() != RegistrationStatus.CANCELLED)
                .toList();

        Map<Long, Long> countsByUserId = registrations.stream()
                .collect(Collectors.groupingBy(r -> r.getUser().getId(), Collectors.counting()));

        return countsByUserId.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    User user = userRepository.findById(entry.getKey()).orElse(null);
                    Map<String, Object> m = new HashMap<>();
                    m.put("studentId", entry.getKey());
                    m.put("studentName", user != null ? user.getName() : "Unknown");
                    m.put("regNumber", user != null ? user.getRegNumber() : "Unknown");
                    m.put("eventCount", entry.getValue());
                    return m;
                })
                .orElseGet(() -> {
                    Map<String, Object> fallback = new HashMap<>();
                    fallback.put("studentName", "N/A");
                    fallback.put("eventCount", 0);
                    return fallback;
                });
    }

    private List<Map<String, Object>> toSortedList(Map<String, Long> counts, String keyName, String valueName) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put(keyName, e.getKey());
                    m.put(valueName, e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }
}
