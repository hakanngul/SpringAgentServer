package com.testautomation.model;

import com.testautomation.model.enums.TestCategory;
import com.testautomation.model.enums.TestPriority;
import com.testautomation.model.enums.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Suite modeli
 * Birden fazla testi gruplandırmak için kullanılır
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_suites")
public class TestSuite {
    @Id
    private String id;
    
    private String name;
    private String description;
    
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    
    @Builder.Default
    private TestStatus status = TestStatus.QUEUED;
    
    @Builder.Default
    private TestSuiteConfiguration configuration = new TestSuiteConfiguration();
    
    @Builder.Default
    private TestSuiteExecutionStrategy executionStrategy = new TestSuiteExecutionStrategy();
    
    @Builder.Default
    private List<TestStep> preconditions = new ArrayList<>();
    
    @Builder.Default
    private List<TestStep> postconditions = new ArrayList<>();
    
    @Builder.Default
    private List<Test> tests = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime queuedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Test Suite yapılandırması
     * Tüm testler için ortak yapılandırma ayarları
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSuiteConfiguration {
        @Builder.Default
        private String browserPreference = "chromium";
        
        @Builder.Default
        private boolean headless = false;
        
        @Builder.Default
        private boolean takeScreenshots = true;
        
        @Builder.Default
        private boolean browserFullScreen = true;
        
        @Builder.Default
        private boolean continueOnFailure = true;
        
        @Builder.Default
        private TestStepOptions stepOptions = TestStepOptions.builder().build();
        
        @Builder.Default
        private Map<String, Object> additionalOptions = new HashMap<>();
    }
    
    /**
     * Test Suite çalıştırma stratejisi
     * Testlerin nasıl çalıştırılacağını belirler
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSuiteExecutionStrategy {
        public enum ExecutionType {
            SEQUENTIAL, PARALLEL
        }
        
        @Builder.Default
        private ExecutionType type = ExecutionType.SEQUENTIAL;
        
        @Builder.Default
        private int maxParallelTests = 3;
        
        @Builder.Default
        private boolean stopOnFirstFailure = false;
        
        @Builder.Default
        private int maxRetries = 0;
        
        @Builder.Default
        private Map<String, Object> additionalOptions = new HashMap<>();
    }
    
    /**
     * Test durumunu günceller
     * @param newStatus Yeni durum
     * @param data Ek veri
     */
    public void updateStatus(TestStatus newStatus, Map<String, Object> data) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        if (data != null) {
            this.metadata.putAll(data);
        }
        
        if (newStatus == TestStatus.QUEUED && this.queuedAt == null) {
            this.queuedAt = LocalDateTime.now();
        } else if (newStatus == TestStatus.RUNNING && this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        } else if (newStatus == TestStatus.COMPLETED || newStatus == TestStatus.FAILED || newStatus == TestStatus.CANCELLED) {
            this.completedAt = LocalDateTime.now();
        }
    }
}
