package com.testautomation.service.core;

import com.testautomation.model.Test;
import com.testautomation.model.TestRequest;
import com.testautomation.model.TestResult;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.repository.TestRepository;
import com.testautomation.repository.TestResultRepository;
import com.testautomation.service.runners.TestRunner;
import com.testautomation.service.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final TestRunner testRunner;
    private final WebSocketService webSocketService;

    public Test createTest(Test test) {
        test.setCreatedAt(LocalDateTime.now());
        test.setStatus(TestStatus.QUEUED);
        return testRepository.save(test);
    }

    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    public Optional<Test> getTestById(String id) {
        return testRepository.findById(id);
    }

    public List<Test> getTestsByStatus(TestStatus status) {
        return testRepository.findByStatus(status);
    }

    public List<Test> getTestsByAgentId(String agentId) {
        return testRepository.findByAgentId(agentId);
    }

    public CompletableFuture<TestResult> runTest(String testId, String agentId) {
        Optional<Test> optionalTest = testRepository.findById(testId);

        if (optionalTest.isPresent()) {
            Test test = optionalTest.get();
            test.setAgentId(agentId);
            test.updateStatus(TestStatus.QUEUED, null);
            testRepository.save(test);
            webSocketService.sendTestStatus(test);

            return testRunner.runTest(test, agentId);
        } else {
            CompletableFuture<TestResult> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Test not found with ID: " + testId));
            return future;
        }
    }

    public CompletableFuture<TestResult> runTest(TestRequest request) {
        Test test = request.getTest();
        String agentId = request.getAgentId();

        // Save the test if it doesn't have an ID
        if (test.getId() == null || test.getId().isEmpty()) {
            test = createTest(test);
        }

        test.setAgentId(agentId);
        test.updateStatus(TestStatus.QUEUED, null);
        testRepository.save(test);
        webSocketService.sendTestStatus(test);

        CompletableFuture<TestResult> future = testRunner.runTest(test, agentId);

        // Handle async request with callback URL
        if (request.isAsync() && request.getCallbackUrl() != null && !request.getCallbackUrl().isEmpty()) {
            future.thenAccept(result -> {
                // Here you would implement the callback logic
                // For example, using RestTemplate to POST the result to the callback URL
                // This is just a placeholder for the implementation
                System.out.println("Test completed, would call back to: " + request.getCallbackUrl());
            });
        }

        return future;
    }

    public CompletableFuture<List<TestResult>> runTests(List<String> testIds, String agentId) {
        Iterable<Test> testsIterable = testRepository.findAllById(testIds);
        List<Test> tests = new ArrayList<>();
        testsIterable.forEach(tests::add);

        for (Test test : tests) {
            test.setAgentId(agentId);
            test.updateStatus(TestStatus.QUEUED, null);
        }

        testRepository.saveAll(tests);
        tests.forEach(webSocketService::sendTestStatus);

        return testRunner.runTests(tests, agentId);
    }

    public Test updateTest(String id, Test updatedTest) {
        return testRepository.findById(id)
            .map(test -> {
                test.setName(updatedTest.getName());
                test.setDescription(updatedTest.getDescription());
                test.setPriority(updatedTest.getPriority());
                test.setCategory(updatedTest.getCategory());
                test.setTags(updatedTest.getTags());
                test.setBrowserPreference(updatedTest.getBrowserPreference());
                test.setHeadless(updatedTest.isHeadless());
                test.setTakeScreenshots(updatedTest.isTakeScreenshots());
                test.setBrowserFullScreen(updatedTest.isBrowserFullScreen());
                test.setSteps(updatedTest.getSteps());
                test.setMetadata(updatedTest.getMetadata());
                test.setPreconditions(updatedTest.getPreconditions());
                test.setExpectedResults(updatedTest.getExpectedResults());

                return testRepository.save(test);
            })
            .orElseThrow(() -> new RuntimeException("Test not found with ID: " + id));
    }

    public void deleteTest(String id) {
        testRepository.deleteById(id);
    }

    public Test cancelTest(String id) {
        return testRepository.findById(id)
            .map(test -> {
                if (test.getStatus() == TestStatus.RUNNING || test.getStatus() == TestStatus.QUEUED) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("error", "Test cancelled by user");
                    test.updateStatus(TestStatus.CANCELLED, data);
                    testRepository.save(test);
                    webSocketService.sendTestStatus(test);
                }
                return test;
            })
            .orElseThrow(() -> new RuntimeException("Test not found with ID: " + id));
    }

    public List<TestResult> getTestResults(String testId) {
        return testResultRepository.findByTestId(testId);
    }
}
