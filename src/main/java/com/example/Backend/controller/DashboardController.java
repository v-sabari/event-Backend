package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.model.User;
import com.example.Backend.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> admin() {
        return ApiResponse.success(dashboardService.adminSummary());
    }

    @GetMapping("/organizer")
    @PreAuthorize("hasRole('STUDENT_ORGANIZER')")
    public ApiResponse<Map<String, Object>> organizer(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(dashboardService.organizerSummary(currentUser));
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Map<String, Object>> student(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(dashboardService.studentSummary(currentUser));
    }
}
