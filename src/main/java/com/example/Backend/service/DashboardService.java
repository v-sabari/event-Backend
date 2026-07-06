package com.example.Backend.service;

import com.example.Backend.model.User;

import java.util.Map;

public interface DashboardService {

    /** Total Users, Total Events, Pending Approvals, Active Events, Today's Events, Recent Registrations. */
    Map<String, Object> adminSummary();

    /** My Events, Pending Approval, Participants, Feedback - scoped to this organizer. */
    Map<String, Object> organizerSummary(User organizer);

    /** Upcoming Events, Registered Events, Certificates, Notifications - scoped to this student. */
    Map<String, Object> studentSummary(User student);
}
