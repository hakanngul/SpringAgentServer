package com.testautomation.service.core;

import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.model.TestSuite;
import com.testautomation.model.TestSuiteResult;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.repository.TestSuiteRepository;
import com.testautomation.repository.TestSuiteResultRepository;
import com.testautomation.service.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Test Suite servisi
 * Test Suite yönetimi için kullanılır
 */
@Service
@RequiredArgsConstructor
public class TestSuiteService {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteService.class);
    
    private final TestSuiteRepository testSuiteRepository;
    private final TestSuiteResultRepository testSuiteResultRepository;
    private final TestService testService;
    private final AgentPoolService agentPoolService;
    private final WebSocketService webSocketService;
    private final TestSuiteRunner testSuiteRunner;
    
    /**
     * Tüm Test Suite'leri getirir
     * @return Test Suite listesi
     */
    public List<TestSuite> getAllTestSuites() {
        return testSuiteRepository.findAll();
    }
    
    /**
     * ID'ye göre Test Suite getirir
     * @param id Test Suite ID
     * @return Test Suite
     */
    public Optional<TestSuite> getTestSuiteById(String id) {
        return testSuiteRepository.findById(id);
    }
    
    /**
     * Yeni Test Suite oluşturur
     * @param testSuite Test Suite
     * @return Oluşturulan Test Suite
     */
    public TestSuite createTestSuite(TestSuite testSuite) {
        testSuite.setCreatedAt(LocalDateTime.now());
        testSuite.setUpdatedAt(LocalDateTime.now());
        testSuite.setStatus(TestStatus.QUEUED);
        
        logger.info("Test Suite oluşturuluyor: {}", testSuite.getName());
        return testSuiteRepository.save(testSuite);
    }
    
    /**
     * Test Suite günceller
     * @param id Test Suite ID
     * @param testSuite Güncellenecek Test Suite
     * @return Güncellenen Test Suite
     */
    public TestSuite updateTestSuite(String id, TestSuite testSuite) {
        return testSuiteRepository.findById(id)
                .map(existingSuite -> {
                    // Sadece değiştirilebilir alanları güncelle
                    existingSuite.setName(testSuite.getName());
                    existingSuite.setDescription(testSuite.getDescription());
                    existingSuite.setTags(testSuite.getTags());
                    existingSuite.setConfiguration(testSuite.getConfiguration());
                    existingSuite.setExecutionStrategy(testSuite.getExecutionStrategy());
                    existingSuite.setPreconditions(testSuite.getPreconditions());
                    existingSuite.setPostconditions(testSuite.getPostconditions());
                    existingSuite.setTests(testSuite.getTests());
                    existingSuite.setUpdatedAt(LocalDateTime.now());
                    
                    logger.info("Test Suite güncelleniyor: {}", existingSuite.getId());
                    return testSuiteRepository.save(existingSuite);
                })
                .orElseThrow(() -> new RuntimeException("Test Suite bulunamadı: " + id));
    }
    
    /**
     * Test Suite siler
     * @param id Test Suite ID
     */
    public void deleteTestSuite(String id) {
        logger.info("Test Suite siliniyor: {}", id);
        testSuiteRepository.deleteById(id);
    }
    
    /**
     * Test Suite çalıştırır
     * @param id Test Suite ID
     * @return Test Suite sonucu için CompletableFuture
     */
    public CompletableFuture<TestSuiteResult> runTestSuite(String id) {
        logger.info("Test Suite çalıştırılıyor: {}", id);
        
        TestSuite testSuite = testSuiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test Suite bulunamadı: " + id));
        
        // Test Suite durumunu güncelle
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Test Suite kuyruğa alındı");
        testSuite.updateStatus(TestStatus.QUEUED, data);
        testSuiteRepository.save(testSuite);
        
        // WebSocket ile durumu bildir
        webSocketService.sendTestSuiteStatus(testSuite);
        
        // Test Suite sonucu oluştur
        TestSuiteResult result = TestSuiteResult.builder()
                .suiteId(testSuite.getId())
                .suiteName(testSuite.getName())
                .suiteDescription(testSuite.getDescription())
                .status(TestStatus.QUEUED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        result = testSuiteResultRepository.save(result);
        
        // Test Suite'i çalıştır
        return testSuiteRunner.runTestSuite(testSuite, result);
    }
    
    /**
     * Test Suite durumunu günceller
     * @param id Test Suite ID
     * @param status Yeni durum
     * @param data Ek veri
     * @return Güncellenen Test Suite
     */
    public TestSuite updateTestSuiteStatus(String id, TestStatus status, Map<String, Object> data) {
        return testSuiteRepository.findById(id)
                .map(testSuite -> {
                    testSuite.updateStatus(status, data);
                    TestSuite updatedSuite = testSuiteRepository.save(testSuite);
                    
                    // WebSocket ile durumu bildir
                    webSocketService.sendTestSuiteStatus(updatedSuite);
                    
                    return updatedSuite;
                })
                .orElseThrow(() -> new RuntimeException("Test Suite bulunamadı: " + id));
    }
    
    /**
     * Test Suite sonuçlarını getirir
     * @param id Test Suite ID
     * @return Test Suite sonuç listesi
     */
    public List<TestSuiteResult> getTestSuiteResults(String id) {
        return testSuiteResultRepository.findBySuiteIdOrderByCreatedAtDesc(id);
    }
    
    /**
     * Test Suite sonucunu getirir
     * @param resultId Test Suite sonuç ID
     * @return Test Suite sonucu
     */
    public Optional<TestSuiteResult> getTestSuiteResultById(String resultId) {
        return testSuiteResultRepository.findById(resultId);
    }
    
    /**
     * Test Suite'i iptal eder
     * @param id Test Suite ID
     * @return İptal edilen Test Suite
     */
    public TestSuite cancelTestSuite(String id) {
        logger.info("Test Suite iptal ediliyor: {}", id);
        
        TestSuite testSuite = testSuiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test Suite bulunamadı: " + id));
        
        // Sadece çalışan veya kuyruktaki Test Suite'ler iptal edilebilir
        if (testSuite.getStatus() != TestStatus.RUNNING && testSuite.getStatus() != TestStatus.QUEUED) {
            throw new RuntimeException("Sadece çalışan veya kuyruktaki Test Suite'ler iptal edilebilir");
        }
        
        // Test Suite durumunu güncelle
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Test Suite iptal edildi");
        testSuite.updateStatus(TestStatus.CANCELLED, data);
        testSuiteRepository.save(testSuite);
        
        // WebSocket ile durumu bildir
        webSocketService.sendTestSuiteStatus(testSuite);
        
        return testSuite;
    }
}
