package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Reports module - restricted to SUPER_ADMIN and FACULTY_COORDINATOR/HOD
 * (institution-level oversight), not individual organizers or students.
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FACULTY_COORDINATOR', 'HOD')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/department-wise-events")
    public ApiResponse<List<Map<String, Object>>> departmentWiseEvents() {
        return ApiResponse.success(reportService.departmentWiseEvents());
    }

    @GetMapping("/category-wise-events")
    public ApiResponse<List<Map<String, Object>>> categoryWiseEvents() {
        return ApiResponse.success(reportService.categoryWiseEvents());
    }

    @GetMapping("/attendance")
    public ApiResponse<List<Map<String, Object>>> attendanceReport() {
        return ApiResponse.success(reportService.attendanceReport());
    }

    @GetMapping("/registrations")
    public ApiResponse<List<Map<String, Object>>> registrationReport() {
        return ApiResponse.success(reportService.registrationReport());
    }

    @GetMapping("/feedback-analysis")
    public ApiResponse<Map<String, Object>> feedbackAnalysis() {
        return ApiResponse.success(reportService.feedbackAnalysis());
    }

    @GetMapping("/most-active-department")
    public ApiResponse<Map<String, Object>> mostActiveDepartment() {
        return ApiResponse.success(reportService.mostActiveDepartment());
    }

    @GetMapping("/most-active-student")
    public ApiResponse<Map<String, Object>> mostActiveStudent() {
        return ApiResponse.success(reportService.mostActiveStudent());
    }
}
