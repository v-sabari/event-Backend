package com.example.Backend.service;

import java.util.List;
import java.util.Map;

public interface ReportService {

    List<Map<String, Object>> departmentWiseEvents();

    List<Map<String, Object>> categoryWiseEvents();

    /** Per-event: registered vs attended count + attendance rate. */
    List<Map<String, Object>> attendanceReport();

    /** Per-event: registered / waitlisted / cancelled / attended breakdown. */
    List<Map<String, Object>> registrationReport();

    /** Per-event average rating + response count, plus an overall average. */
    Map<String, Object> feedbackAnalysis();

    Map<String, Object> mostActiveDepartment();

    Map<String, Object> mostActiveStudent();
}
