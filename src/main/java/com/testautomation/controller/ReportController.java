package com.testautomation.controller;

import com.testautomation.service.core.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    
    @GetMapping("/test-summary")
    public ResponseEntity<Map<String, Object>> getTestSummary() {
        Map<String, Object> summary = reportService.getTestSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/result-summary")
    public ResponseEntity<Map<String, Object>> getTestResultSummary() {
        Map<String, Object> summary = reportService.getTestResultSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getTestResultsByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        Map<String, Object> summary = reportService.getTestResultsByDateRange(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}
