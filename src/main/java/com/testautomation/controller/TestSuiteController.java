package com.testautomation.controller;

import com.testautomation.model.TestSuite;
import com.testautomation.model.TestSuiteResult;
import com.testautomation.service.core.TestSuiteService;
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

/**
 * Test Suite kontrolcüsü
 * Test Suite yönetimi için API uç noktaları sağlar
 */
@RestController
@RequestMapping("/api/test-suites")
@RequiredArgsConstructor
@Tag(name = "Test Suite Yönetimi", description = "Test Suite oluşturma, çalıştırma ve yönetme API'leri")
public class TestSuiteController {
    private final TestSuiteService testSuiteService;
    
    @Operation(summary = "Tüm Test Suite'leri getir", description = "Sistemdeki tüm Test Suite'leri listeler")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test Suite'ler başarıyla getirildi",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestSuite.class)))
    })
    @GetMapping
    public ResponseEntity<List<TestSuite>> getAllTestSuites() {
        List<TestSuite> testSuites = testSuiteService.getAllTestSuites();
        return ResponseEntity.ok(testSuites);
    }
    
    @Operation(summary = "ID'ye göre Test Suite getir", description = "Belirtilen ID'ye sahip Test Suite'i getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test Suite başarıyla getirildi",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestSuite.class))),
        @ApiResponse(responseCode = "404", description = "Test Suite bulunamadı", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TestSuite> getTestSuiteById(
            @Parameter(description = "Test Suite ID", required = true) @PathVariable String id) {
        return testSuiteService.getTestSuiteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Yeni Test Suite oluştur", description = "Yeni bir Test Suite oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Test Suite başarıyla oluşturuldu",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestSuite.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TestSuite> createTestSuite(
            @Parameter(description = "Test Suite", required = true) @RequestBody TestSuite testSuite) {
        TestSuite createdTestSuite = testSuiteService.createTestSuite(testSuite);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTestSuite);
    }
    
    @Operation(summary = "Test Suite güncelle", description = "Belirtilen ID'ye sahip Test Suite'i günceller")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test Suite başarıyla güncellendi",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestSuite.class))),
        @ApiResponse(responseCode = "404", description = "Test Suite bulunamadı", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<TestSuite> updateTestSuite(
            @Parameter(description = "Test Suite ID", required = true) @PathVariable String id,
            @Parameter(description = "Test Suite", required = true) @RequestBody TestSuite testSuite) {
        try {
            TestSuite updatedTestSuite = testSuiteService.updateTestSuite(id, testSuite);
            return ResponseEntity.ok(updatedTestSuite);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Test Suite sil", description = "Belirtilen ID'ye sahip Test Suite'i siler")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Test Suite başarıyla silindi", content = @Content),
        @ApiResponse(responseCode = "404", description = "Test Suite bulunamadı", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestSuite(
            @Parameter(description = "Test Suite ID", required = true) @PathVariable String id) {
        try {
            testSuiteService.deleteTestSuite(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Test Suite çalıştır", description = "Belirtilen ID'ye sahip Test Suite'i çalıştırır")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Test Suite çalıştırma isteği kabul edildi",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompletableFuture.class))),
        @ApiResponse(responseCode = "404", description = "Test Suite bulunamadı", content = @Content)
    })
    @PostMapping("/{id}/run")
    public ResponseEntity<CompletableFuture<TestSuiteResult>> runTestSuite(
            @Parameter(description = "Test Suite ID", required = true) @PathVariable String id) {
        try {
            CompletableFuture<TestSuiteResult> future = testSuiteService.runTestSuite(id);
            return ResponseEntity.accepted().body(future);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Test Suite iptal et", description = "Belirtilen ID'ye sahip Test Suite'i iptal eder")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test Suite başarıyla iptal edildi",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestSuite.class))),
        @ApiResponse(responseCode = "404", description = "Test Suite bulunamadı", content = @Content)
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TestSuite> cancelTestSuite(
            @Parameter(description = "Test Suite ID", required = true) @PathVariable String id) {
        try {
            TestSuite cancelledTestSuite = testSuiteService.cancelTestSuite(id);
            return ResponseEntity.ok(cancelledTestSuite);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Test Suite sonuçlarını getir", description = "Belirtilen ID'ye sahip Test Suite'in sonuçlarını getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test Suite sonuçları başarıyla getirildi",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestSuiteResult.class))),
        @ApiResponse(responseCode = "404", description = "Test Suite bulunamadı", content = @Content)
    })
    @GetMapping("/{id}/results")
    public ResponseEntity<List<TestSuiteResult>> getTestSuiteResults(
            @Parameter(description = "Test Suite ID", required = true) @PathVariable String id) {
        try {
            List<TestSuiteResult> results = testSuiteService.getTestSuiteResults(id);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Test Suite sonucunu getir", description = "Belirtilen ID'ye sahip Test Suite sonucunu getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test Suite sonucu başarıyla getirildi",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestSuiteResult.class))),
        @ApiResponse(responseCode = "404", description = "Test Suite sonucu bulunamadı", content = @Content)
    })
    @GetMapping("/results/{resultId}")
    public ResponseEntity<TestSuiteResult> getTestSuiteResultById(
            @Parameter(description = "Test Suite sonuç ID", required = true) @PathVariable String resultId) {
        return testSuiteService.getTestSuiteResultById(resultId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
