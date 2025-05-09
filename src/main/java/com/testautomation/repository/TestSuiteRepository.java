package com.testautomation.repository;

import com.testautomation.model.TestSuite;
import com.testautomation.model.enums.TestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Test Suite repository
 * Test Suite veritabanı işlemleri için kullanılır
 */
@Repository
public interface TestSuiteRepository extends MongoRepository<TestSuite, String> {
    
    /**
     * Duruma göre Test Suite'leri bulur
     * @param status Test Suite durumu
     * @return Test Suite listesi
     */
    List<TestSuite> findByStatus(TestStatus status);
    
    /**
     * Etiketlere göre Test Suite'leri bulur
     * @param tag Etiket
     * @return Test Suite listesi
     */
    List<TestSuite> findByTagsContaining(String tag);
    
    /**
     * Belirli bir tarihten sonra oluşturulan Test Suite'leri bulur
     * @param date Tarih
     * @return Test Suite listesi
     */
    List<TestSuite> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Belirli bir tarihten önce oluşturulan Test Suite'leri bulur
     * @param date Tarih
     * @return Test Suite listesi
     */
    List<TestSuite> findByCreatedAtBefore(LocalDateTime date);
    
    /**
     * İsme göre Test Suite'leri bulur (içeren)
     * @param name İsim
     * @return Test Suite listesi
     */
    List<TestSuite> findByNameContaining(String name);
    
    /**
     * Açıklamaya göre Test Suite'leri bulur (içeren)
     * @param description Açıklama
     * @return Test Suite listesi
     */
    List<TestSuite> findByDescriptionContaining(String description);
    
    /**
     * Duruma ve etiketlere göre Test Suite'leri bulur
     * @param status Durum
     * @param tag Etiket
     * @return Test Suite listesi
     */
    List<TestSuite> findByStatusAndTagsContaining(TestStatus status, String tag);
}
