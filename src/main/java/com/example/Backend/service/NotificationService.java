package com.example.Backend.service;

import com.example.Backend.model.Notification;
import com.example.Backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Creates the in-app notification and, if sendEmail is true, also sends
     * an email via the existing EmailService - one call covers both channels
     * so callers (approval workflow, registration, certificates...) don't
     * have to invoke two services separately.
     */
    void notify(User recipient, String title, String message, String type,
                String relatedEntityType, Long relatedEntityId, boolean sendEmail);

    Page<Notification> findForUser(Long userId, Pageable pageable);

    long countUnread(Long userId);

    void markRead(Long notificationId, User currentUser);

    void markAllRead(Long userId);
}
