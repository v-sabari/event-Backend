package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.feedback.FeedbackRequestDTO;
import com.example.Backend.dto.feedback.FeedbackResponseDTO;
import com.example.Backend.model.Feedback;
import com.example.Backend.model.User;
import com.example.Backend.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<FeedbackResponseDTO> submit(@PathVariable Long eventId,
                                                    @Valid @RequestBody FeedbackRequestDTO dto,
                                                    @AuthenticationPrincipal User currentUser) {
        Feedback feedback = feedbackService.submit(eventId, dto, currentUser);
        return ApiResponse.success("Feedback submitted", FeedbackResponseDTO.from(feedback));
    }

    @GetMapping
    public ApiResponse<List<FeedbackResponseDTO>> list(@PathVariable Long eventId) {
        List<FeedbackResponseDTO> response = feedbackService.findByEvent(eventId).stream()
                .map(FeedbackResponseDTO::from).toList();
        return ApiResponse.success(response);
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@PathVariable Long eventId) {
        Double avg = feedbackService.averageRating(eventId);
        int count = feedbackService.findByEvent(eventId).size();
        return ApiResponse.success(Map.of("averageRating", avg != null ? avg : 0.0, "totalResponses", count));
    }
}
