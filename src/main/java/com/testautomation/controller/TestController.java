package com.testautomation.controller;

import com.testautomation.model.Test;
import com.testautomation.model.TestRequest;
import com.testautomation.model.TestResult;
import com.testautomation.service.core.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@Tag(name = "Test Yönetimi", description = "Test oluşturma, çalıştırma ve yönetme API'leri")
public class TestController {
    private final TestService testService;

    @Operation(summary = "Yeni test oluştur", description = "Yeni bir test senaryosu oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Test başarıyla oluşturuldu",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Test.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content)
    })
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

    @PostMapping("/run")
    public ResponseEntity<CompletableFuture<TestResult>> runTest(
        @RequestBody TestRequest request
    ) {
        CompletableFuture<TestResult> future = testService.runTest(request);
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
