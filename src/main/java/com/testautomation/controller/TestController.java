package com.testautomation.controller;

import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.service.core.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;
    
    @PostMapping
    public ResponseEntity<Test> createTest(@RequestBody Test test) {
        Test createdTest = testService.createTest(test);
        return new ResponseEntity<>(createdTest, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<Test>> getAllTests() {
        List<Test> tests = testService.getAllTests();
        return ResponseEntity.ok(tests);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable String id) {
        return testService.getTestById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Test> updateTest(@PathVariable String id, @RequestBody Test test) {
        try {
            Test updatedTest = testService.updateTest(id, test);
            return ResponseEntity.ok(updatedTest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable String id) {
        testService.deleteTest(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/run")
    public ResponseEntity<CompletableFuture<TestResult>> runTest(
        @PathVariable String id, 
        @RequestParam String agentId
    ) {
        CompletableFuture<TestResult> future = testService.runTest(id, agentId);
        return ResponseEntity.accepted().body(future);
    }
    
    @PostMapping("/run-batch")
    public ResponseEntity<CompletableFuture<List<TestResult>>> runTests(
        @RequestBody List<String> testIds, 
        @RequestParam String agentId
    ) {
        CompletableFuture<List<TestResult>> future = testService.runTests(testIds, agentId);
        return ResponseEntity.accepted().body(future);
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Test> cancelTest(@PathVariable String id) {
        try {
            Test cancelledTest = testService.cancelTest(id);
            return ResponseEntity.ok(cancelledTest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/results")
    public ResponseEntity<List<TestResult>> getTestResults(@PathVariable String id) {
        List<TestResult> results = testService.getTestResults(id);
        return ResponseEntity.ok(results);
    }
}
