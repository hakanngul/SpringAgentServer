package com.testautomation.repository;

import com.testautomation.model.TestSuiteResult;
import com.testautomation.model.enums.TestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Test Suite sonuç repository
 * Test Suite sonuçları veritabanı işlemleri için kullanılır
 */
@Repository
public interface TestSuiteResultRepository extends MongoRepository<TestSuiteResult, String> {
    
    /**
     * Test Suite ID'sine göre sonuçları bulur
     * @param suiteId Test Suite ID
     * @return Test Suite sonuç listesi
     */
    List<TestSuiteResult> findBySuiteId(String suiteId);
    
    /**
     * Test Suite ID'sine göre sonuçları bulur (en son oluşturulana göre sıralı)
     * @param suiteId Test Suite ID
     * @return Test Suite sonuç listesi
     */
    List<TestSuiteResult> findBySuiteIdOrderByCreatedAtDesc(String suiteId);
    
    /**
     * Duruma göre sonuçları bulur
     * @param status Durum
     * @return Test Suite sonuç listesi
     */
    List<TestSuiteResult> findByStatus(TestStatus status);
    
    /**
     * Agent ID'sine göre sonuçları bulur
     * @param agentId Agent ID
     * @return Test Suite sonuç listesi
     */
    List<TestSuiteResult> findByAgentId(String agentId);
    
    /**
     * Belirli bir tarih aralığında başlayan sonuçları bulur
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Test Suite sonuç listesi
     */
    List<TestSuiteResult> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Belirli bir tarih aralığında biten sonuçları bulur
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Test Suite sonuç listesi
     */
    List<TestSuiteResult> findByEndTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Test Suite ID'sine ve duruma göre sonuçları bulur
     * @param suiteId Test Suite ID
     * @param status Durum
     * @return Test Suite sonuç listesi
     */
    List<TestSuiteResult> findBySuiteIdAndStatus(String suiteId, TestStatus status);
    
    /**
     * Belirli bir tarihten sonra oluşturulan sonuçları bulur
     * @param date Tarih
     * @return Test Suite sonuç listesi
     */
    List<TestSuiteResult> findByCreatedAtAfter(LocalDateTime date);
}
