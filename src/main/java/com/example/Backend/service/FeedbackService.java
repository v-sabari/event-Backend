package com.example.Backend.service;

import com.example.Backend.dto.feedback.FeedbackRequestDTO;
import com.example.Backend.model.Feedback;
import com.example.Backend.model.User;

import java.util.List;

public interface FeedbackService {

    /** Only a student who actually attended the event may leave feedback, once. */
    Feedback submit(Long eventId, FeedbackRequestDTO dto, User student);

    List<Feedback> findByEvent(Long eventId);

    Double averageRating(Long eventId);
}
