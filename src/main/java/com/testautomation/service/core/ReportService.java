package com.testautomation.service.core;

import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.model.enums.TestCategory;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.repository.TestRepository;
import com.testautomation.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    
    public Map<String, Object> getTestSummary() {
        List<Test> allTests = testRepository.findAll();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTests", allTests.size());
        
        // Count by status
        Map<TestStatus, Long> statusCounts = allTests.stream()
            .collect(Collectors.groupingBy(Test::getStatus, Collectors.counting()));
        summary.put("statusCounts", statusCounts);
        
        // Count by category
        Map<TestCategory, Long> categoryCounts = allTests.stream()
            .collect(Collectors.groupingBy(Test::getCategory, Collectors.counting()));
        summary.put("categoryCounts", categoryCounts);
        
        return summary;
    }
    
    public Map<String, Object> getTestResultSummary() {
        List<TestResult> allResults = testResultRepository.findAll();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalResults", allResults.size());
        
        // Count by success
        long successCount = allResults.stream().filter(TestResult::isSuccess).count();
        long failureCount = allResults.size() - successCount;
        
        summary.put("successCount", successCount);
        summary.put("failureCount", failureCount);
        summary.put("successRate", allResults.isEmpty() ? 0 : (double) successCount / allResults.size());
        
        return summary;
    }
    
    public Map<String, Object> getTestResultsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Test> tests = testRepository.findAll();
        
        List<Test> testsInRange = tests.stream()
            .filter(test -> {
                LocalDateTime completedAt = test.getCompletedAt();
                return completedAt != null && 
                       !completedAt.isBefore(startDate) && 
                       !completedAt.isAfter(endDate);
            })
            .collect(Collectors.toList());
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTests", testsInRange.size());
        
        // Count by status
        Map<TestStatus, Long> statusCounts = testsInRange.stream()
            .collect(Collectors.groupingBy(Test::getStatus, Collectors.counting()));
        summary.put("statusCounts", statusCounts);
        
        return summary;
    }
}
