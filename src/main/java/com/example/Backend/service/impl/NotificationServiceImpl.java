package com.example.Backend.service.impl;

import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.Notification;
import com.example.Backend.model.User;
import com.example.Backend.repository.NotificationRepository;
import com.example.Backend.service.EmailService;
import com.example.Backend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void notify(User recipient, String title, String message, String type,
                        String relatedEntityType, Long relatedEntityId, boolean sendEmail) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notificationRepository.save(notification);

        log.debug("In-app notification created for {}: {}", recipient.getRegNumber(), title);

        if (sendEmail) {
            emailService.sendEmail(recipient.getEmail(), title, message);
        }
    }

    @Override
    public Page<Notification> findForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedCustomException("You cannot modify another user's notifications");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
