package com.testautomation.controller;

import com.testautomation.service.core.TestQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TestQueueController
 * Test kuyruğu API'leri
 */
@RestController
@RequestMapping("/api/tests/queue")
@RequiredArgsConstructor
public class TestQueueController {
    private final TestQueueService testQueueService;

    /**
     * Test kuyruğu durumunu al
     * @return Test kuyruğu durumu
     */
    @GetMapping("/status")
    public ResponseEntity<TestQueueService.QueueStatus> getQueueStatus() {
        TestQueueService.QueueStatus status = testQueueService.getQueueStatus();
        return ResponseEntity.ok(status);
    }
}
