package com.example.Backend.service;

import com.example.Backend.model.Event;
import com.example.Backend.model.EventApprovalHistory;
import com.example.Backend.model.User;

import java.util.List;

public interface EventApprovalService {

    /** DRAFT/REJECTED -> PENDING_FACULTY_APPROVAL. Only the event's creator (or admin) may submit. */
    Event submit(Long eventId, User currentUser);

    /** Advances to the next stage, or to PUBLISHED if this was the last required stage. */
    Event approve(Long eventId, User approver, String remarks);

    /** Moves the event to REJECTED. Remarks are mandatory (Reject with Remarks). */
    Event reject(Long eventId, User approver, String remarks);

    List<EventApprovalHistory> getHistory(Long eventId);

    /** Events currently awaiting action from this approver's stage (Faculty/HOD/Admin queues). */
    List<Event> findPendingFor(User approver);
}
