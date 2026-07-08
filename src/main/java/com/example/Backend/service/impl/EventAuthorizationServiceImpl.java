package com.example.Backend.service.impl;

import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.model.Event;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.service.EventAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class EventAuthorizationServiceImpl implements EventAuthorizationService {

    @Override
    public boolean canActOnEvent(Event event, User currentUser) {
        if (currentUser.getRole() == Role.STUDENT_ORGANIZER) {
            return event.getCreatedBy().getId().equals(currentUser.getId());
        }
        return currentUser.getRole() == Role.FACULTY_COORDINATOR
                || currentUser.getRole() == Role.HOD
                || currentUser.getRole() == Role.SUPER_ADMIN;
    }

    @Override
    public void assertCanActOnEvent(Event event, User currentUser, String message) {
        if (!canActOnEvent(event, currentUser)) {
            throw new AccessDeniedCustomException(message);
        }
    }
}