package com.testautomation.model;

import com.testautomation.model.enums.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Suite sonuç modeli
 * Bir Test Suite'in çalıştırılması sonucunda oluşan sonuçları içerir
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_suite_results")
public class TestSuiteResult {
    @Id
    private String id;
    
    private String suiteId;
    private String suiteName;
    private String suiteDescription;
    
    @Builder.Default
    private TestStatus status = TestStatus.QUEUED;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Builder.Default
    private List<TestResult> testResults = new ArrayList<>();
    
    @Builder.Default
    private List<TestStepResult> preconditionResults = new ArrayList<>();
    
    @Builder.Default
    private List<TestStepResult> postconditionResults = new ArrayList<>();
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    private String agentId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Toplam test sayısını döndürür
     * @return Toplam test sayısı
     */
    public int getTotalTests() {
        return testResults.size();
    }
    
    /**
     * Başarılı test sayısını döndürür
     * @return Başarılı test sayısı
     */
    public int getSuccessfulTests() {
        return (int) testResults.stream()
                .filter(result -> result.getStatus() == TestStatus.COMPLETED)
                .count();
    }
    
    /**
     * Başarısız test sayısını döndürür
     * @return Başarısız test sayısı
     */
    public int getFailedTests() {
        return (int) testResults.stream()
                .filter(result -> result.getStatus() == TestStatus.FAILED)
                .count();
    }
    
    /**
     * İptal edilen test sayısını döndürür
     * @return İptal edilen test sayısı
     */
    public int getCancelledTests() {
        return (int) testResults.stream()
                .filter(result -> result.getStatus() == TestStatus.CANCELLED)
                .count();
    }
    
    /**
     * Kuyruktaki test sayısını döndürür
     * @return Kuyruktaki test sayısı
     */
    public int getQueuedTests() {
        return (int) testResults.stream()
                .filter(result -> result.getStatus() == TestStatus.QUEUED)
                .count();
    }
    
    /**
     * Çalışan test sayısını döndürür
     * @return Çalışan test sayısı
     */
    public int getRunningTests() {
        return (int) testResults.stream()
                .filter(result -> result.getStatus() == TestStatus.RUNNING)
                .count();
    }
    
    /**
     * Toplam süreyi döndürür
     * @return Toplam süre (milisaniye)
     */
    public long getTotalDuration() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).toMillis();
    }
    
    /**
     * Başarı oranını döndürür
     * @return Başarı oranı (0-100)
     */
    public double getSuccessRate() {
        if (getTotalTests() == 0) {
            return 0;
        }
        return (double) getSuccessfulTests() / getTotalTests() * 100;
    }
    
    /**
     * Test Suite sonucunu günceller
     * @param newStatus Yeni durum
     * @param data Ek veri
     */
    public void updateStatus(TestStatus newStatus, Map<String, Object> data) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        if (data != null) {
            this.metadata.putAll(data);
        }
        
        if (newStatus == TestStatus.RUNNING && this.startTime == null) {
            this.startTime = LocalDateTime.now();
        } else if (newStatus == TestStatus.COMPLETED || newStatus == TestStatus.FAILED || newStatus == TestStatus.CANCELLED) {
            this.endTime = LocalDateTime.now();
        }
    }
}
