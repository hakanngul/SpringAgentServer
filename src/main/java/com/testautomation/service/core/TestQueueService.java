package com.testautomation.service.core;

import com.testautomation.model.Test;
import com.testautomation.model.TestStatusInfo;
import com.testautomation.model.enums.TestPriority;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.service.websocket.WebSocketService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TestQueueService {
    private static final Logger logger = LoggerFactory.getLogger(TestQueueService.class);
    
    private final WebSocketService webSocketService;
    
    @Value("${app.queue.max-size:100}")
    private int maxSize;
    
    @Value("${app.queue.timeout:1800000}") // Default 30 minutes
    private long timeout;
    
    private final List<String> queue = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Test> tests = new ConcurrentHashMap<>();
    
    /**
     * Add a test to the queue
     * @param test The test to add
     * @return true if successful, false otherwise
     */
    public boolean addTest(Test test) {
        // Check queue size
        if (queue.size() >= maxSize) {
            logger.warn("Queue is full: {}/{}", queue.size(), maxSize);
            return false;
        }
        
        // Add test to queue
        queue.add(test.getId());
        tests.put(test.getId(), test);
        
        // Sort queue by priority
        sortQueueByPriority();
        
        // Get test's new queue position
        int queuePosition = getQueuePosition(test.getId());
        
        logger.info("Test added to queue: {} ({}) - Priority: {} - Position: {}", 
                test.getId(), test.getName(), test.getPriority(), queuePosition + 1);
        
        // Send test status update
        webSocketService.sendTestStatus(test);
        
        return true;
    }
    
    /**
     * Get the next test in the queue
     * @return The next test or null if queue is empty
     */
    public Test getNextTest() {
        if (queue.isEmpty()) {
            return null;
        }
        
        // Sort queue by priority
        sortQueueByPriority();
        
        // Get the first test in the queue
        String testId = queue.remove(0);
        
        if (!tests.containsKey(testId)) {
            return null;
        }
        
        Test test = tests.get(testId);
        
        if (test != null) {
            logger.info("Next test retrieved: {} ({}) - Priority: {}", 
                    test.getId(), test.getName(), test.getPriority());
        }
        
        return test;
    }
    
    /**
     * Update test status
     * @param testId Test ID
     * @param status New status
     * @param data Additional data
     * @return true if successful, false otherwise
     */
    public boolean updateTestStatus(String testId, TestStatus status, Map<String, Object> data) {
        if (!tests.containsKey(testId)) {
            return false;
        }
        
        Test test = tests.get(testId);
        
        if (test != null) {
            TestStatus oldStatus = test.getStatus();
            test.updateStatus(status, data);
            
            logger.info("Test status changed: {} ({}) - {} -> {}", 
                    test.getId(), test.getName(), oldStatus, status);
            
            // Send test status update
            webSocketService.sendTestStatus(test);
            
            // If test is completed, failed, timed out, or cancelled, remove from queue
            if (status == TestStatus.COMPLETED || status == TestStatus.FAILED || 
                    status == TestStatus.TIMEOUT || status == TestStatus.CANCELLED) {
                removeFromQueue(test.getId());
                
                // Schedule test cleanup after 5 minutes
                final String finalTestId = testId;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Check if test still exists before removing
                        if (tests.containsKey(finalTestId)) {
                            logger.debug("Cleaning up test object: {}", finalTestId);
                            // Uncomment to actually remove the test
                            // tests.remove(finalTestId);
                        }
                    }
                }, 5 * 60 * 1000); // 5 minutes
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Get test status
     * @param testId Test ID
     * @return Test status or null if not found
     */
    public TestStatusInfo getTestStatus(String testId) {
        if (!tests.containsKey(testId)) {
            return null;
        }
        
        Test test = tests.get(testId);
        return test != null ? new TestStatusInfo(test) : null;
    }
    
    /**
     * Get all test statuses
     * @return Map of test IDs to test statuses
     */
    public Map<String, TestStatusInfo> getAllTestStatuses() {
        Map<String, TestStatusInfo> statuses = new HashMap<>();
        
        for (Map.Entry<String, Test> entry : tests.entrySet()) {
            statuses.put(entry.getKey(), new TestStatusInfo(entry.getValue()));
        }
        
        return statuses;
    }
    
    /**
     * Get queue status
     * @return Queue status
     */
    public QueueStatus getQueueStatus() {
        List<QueuedTest> queuedTests = new ArrayList<>();
        
        for (String testId : queue) {
            Test test = tests.get(testId);
            
            if (test != null) {
                queuedTests.add(new QueuedTest(
                        test.getId(),
                        test.getName(),
                        test.getStatus(),
                        test.getQueuedAt()
                ));
            }
        }
        
        return new QueueStatus(queue.size(), maxSize, queuedTests);
    }
    
    /**
     * Get test's position in queue
     * @param testId Test ID
     * @return Queue position (0-based, -1 if not in queue)
     */
    public int getQueuePosition(String testId) {
        return queue.indexOf(testId);
    }
    
    /**
     * Change test priority
     * @param testId Test ID
     * @param priority New priority
     * @return true if successful, false otherwise
     */
    public boolean prioritizeTest(String testId, TestPriority priority) {
        if (!tests.containsKey(testId)) {
            logger.warn("Test not found: {}", testId);
            return false;
        }
        
        Test test = tests.get(testId);
        
        if (test == null) {
            return false;
        }
        
        // Update test priority
        TestPriority oldPriority = test.getPriority();
        test.setPriority(priority);
        
        logger.info("Test priority changed: {} ({}) - {} -> {}", 
                test.getId(), test.getName(), oldPriority, priority);
        
        // Sort queue by priority
        sortQueueByPriority();
        
        // Send test status update
        webSocketService.sendTestStatus(test);
        
        return true;
    }
    
    /**
     * Clear the queue
     */
    public void clearQueue() {
        // Cancel all tests in queue
        for (String testId : queue) {
            Test test = tests.get(testId);
            
            if (test != null && test.getStatus() == TestStatus.QUEUED) {
                Map<String, Object> data = new HashMap<>();
                data.put("error", "Test queue cleared");
                test.updateStatus(TestStatus.CANCELLED, data);
                
                logger.info("Test cancelled: {} ({})", test.getId(), test.getName());
                
                // Send test status update
                webSocketService.sendTestStatus(test);
            }
        }
        
        // Clear queue
        queue.clear();
        
        logger.info("Queue cleared");
    }
    
    /**
     * Remove test from queue
     * @param testId Test ID
     */
    private void removeFromQueue(String testId) {
        queue.remove(testId);
    }
    
    /**
     * Sort queue by priority
     */
    private void sortQueueByPriority() {
        // Priority order: CRITICAL > HIGH > MEDIUM > LOW
        Map<TestPriority, Integer> priorityOrder = new HashMap<>();
        priorityOrder.put(TestPriority.CRITICAL, 0);
        priorityOrder.put(TestPriority.HIGH, 1);
        priorityOrder.put(TestPriority.MEDIUM, 2);
        priorityOrder.put(TestPriority.LOW, 3);
        
        // Sort queue by priority
        synchronized (queue) {
            queue.sort((a, b) -> {
                Test testA = tests.get(a);
                Test testB = tests.get(b);
                
                if (testA == null || testB == null) {
                    return 0;
                }
                
                // Sort by priority
                int priorityA = priorityOrder.getOrDefault(testA.getPriority(), 4);
                int priorityB = priorityOrder.getOrDefault(testB.getPriority(), 4);
                
                // If priorities are equal, sort by queued time (FIFO)
                if (priorityA == priorityB) {
                    return testA.getQueuedAt().compareTo(testB.getQueuedAt());
                }
                
                return priorityA - priorityB;
            });
        }
        
        logger.debug("Queue sorted by priority");
    }
    
    /**
     * Check for test timeouts
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkTestTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        
        // Check tests in queue
        for (String testId : new ArrayList<>(queue)) {
            Test test = tests.get(testId);
            
            if (test != null && test.getStatus() == TestStatus.QUEUED && test.getQueuedAt() != null) {
                long queueTime = java.time.Duration.between(test.getQueuedAt(), now).toMillis();
                
                if (queueTime > timeout) {
                    // Test timed out
                    Map<String, Object> data = new HashMap<>();
                    data.put("error", "Test waited too long in queue");
                    test.updateStatus(TestStatus.TIMEOUT, data);
                    
                    logger.warn("Test timed out: {} ({}) - {}ms", 
                            test.getId(), test.getName(), queueTime);
                    
                    // Send test status update
                    webSocketService.sendTestStatus(test);
                    
                    // Remove test from queue
                    removeFromQueue(test.getId());
                }
            }
        }
    }
    
    /**
     * Queue status class
     */
    @Getter
    public static class QueueStatus {
        private final int length;
        private final int maxSize;
        private final List<QueuedTest> tests;
        
        public QueueStatus(int length, int maxSize, List<QueuedTest> tests) {
            this.length = length;
            this.maxSize = maxSize;
            this.tests = tests;
        }
    }
    
    /**
     * Queued test class
     */
    @Getter
    public static class QueuedTest {
        private final String id;
        private final String name;
        private final TestStatus status;
        private final LocalDateTime queuedAt;
        
        public QueuedTest(String id, String name, TestStatus status, LocalDateTime queuedAt) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.queuedAt = queuedAt;
        }
    }
}