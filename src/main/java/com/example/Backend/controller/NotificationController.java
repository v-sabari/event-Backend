package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.notification.NotificationResponseDTO;
import com.example.Backend.model.User;
import com.example.Backend.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<Page<NotificationResponseDTO>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<NotificationResponseDTO> result = notificationService
                .findForUser(currentUser.getId(), PageRequest.of(page, size))
                .map(NotificationResponseDTO::from);
        return ApiResponse.success(result);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(Map.of("unreadCount", notificationService.countUnread(currentUser.getId())));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        notificationService.markRead(id, currentUser);
        return ApiResponse.message("Notification marked as read");
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal User currentUser) {
        notificationService.markAllRead(currentUser.getId());
        return ApiResponse.message("All notifications marked as read");
    }
}
