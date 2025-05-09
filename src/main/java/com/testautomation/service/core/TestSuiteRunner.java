package com.testautomation.service.core;

import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.model.TestStepOptions;
import com.testautomation.model.TestStepResult;
import com.testautomation.model.TestSuite;
import com.testautomation.model.TestSuiteResult;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.repository.TestSuiteRepository;
import com.testautomation.repository.TestSuiteResultRepository;
import com.testautomation.service.runners.TestRunner;
import com.testautomation.service.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Test Suite çalıştırıcı
 * Test Suite'leri çalıştırmak için kullanılır
 */
@Service
@RequiredArgsConstructor
public class TestSuiteRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteRunner.class);

    private final TestSuiteRepository testSuiteRepository;
    private final TestSuiteResultRepository testSuiteResultRepository;
    private final TestService testService;
    private final TestRunner testRunner;
    private final AgentPoolService agentPoolService;
    private final WebSocketService webSocketService;

    /**
     * Test Suite'i asenkron olarak çalıştırır
     * @param testSuite Test Suite
     * @param result Test Suite sonucu
     * @return Test Suite sonucu için CompletableFuture
     */
    @Async("taskExecutor")
    public CompletableFuture<TestSuiteResult> runTestSuite(TestSuite testSuite, TestSuiteResult result) {
        logger.info("Test Suite çalıştırılıyor: {} ({})", testSuite.getId(), testSuite.getName());

        try {
            // Test Suite durumunu güncelle
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Test Suite çalıştırılıyor");
            testSuite.updateStatus(TestStatus.RUNNING, data);
            testSuiteRepository.save(testSuite);

            // Test Suite sonucunu güncelle
            result.updateStatus(TestStatus.RUNNING, data);
            result = testSuiteResultRepository.save(result);

            // WebSocket ile durumu bildir
            webSocketService.sendTestSuiteStatus(testSuite);
            webSocketService.sendTestSuiteResult(result);

            // Ön koşulları çalıştır
            List<TestStepResult> preconditionResults = runPreconditions(testSuite);
            result.setPreconditionResults(preconditionResults);
            result = testSuiteResultRepository.save(result);

            // Testleri çalıştır
            boolean success = runTests(testSuite, result);

            // Son koşulları çalıştır
            List<TestStepResult> postconditionResults = runPostconditions(testSuite);
            result.setPostconditionResults(postconditionResults);

            // Test Suite durumunu güncelle
            TestStatus finalStatus = success ? TestStatus.COMPLETED : TestStatus.FAILED;
            data = new HashMap<>();
            data.put("message", success ? "Test Suite başarıyla tamamlandı" : "Test Suite başarısız oldu");
            testSuite.updateStatus(finalStatus, data);
            testSuiteRepository.save(testSuite);

            // Test Suite sonucunu güncelle
            result.updateStatus(finalStatus, data);
            result = testSuiteResultRepository.save(result);

            // WebSocket ile durumu bildir
            webSocketService.sendTestSuiteStatus(testSuite);
            webSocketService.sendTestSuiteResult(result);

            logger.info("Test Suite tamamlandı: {} ({}), Durum: {}", testSuite.getId(), testSuite.getName(), finalStatus);

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            logger.error("Test Suite çalıştırılırken hata oluştu: {} ({})", testSuite.getId(), testSuite.getName(), e);

            // Test Suite durumunu güncelle
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Test Suite çalıştırılırken hata oluştu: " + e.getMessage());
            data.put("error", e.getMessage());
            testSuite.updateStatus(TestStatus.FAILED, data);
            testSuiteRepository.save(testSuite);

            // Test Suite sonucunu güncelle
            result.updateStatus(TestStatus.FAILED, data);
            result = testSuiteResultRepository.save(result);

            // WebSocket ile durumu bildir
            webSocketService.sendTestSuiteStatus(testSuite);
            webSocketService.sendTestSuiteResult(result);

            return CompletableFuture.completedFuture(result);
        }
    }

    /**
     * Ön koşulları çalıştırır
     * @param testSuite Test Suite
     * @return Ön koşul sonuçları
     */
    private List<TestStepResult> runPreconditions(TestSuite testSuite) {
        logger.info("Ön koşullar çalıştırılıyor: {} ({})", testSuite.getId(), testSuite.getName());

        List<TestStepResult> results = new ArrayList<>();

        // Ön koşullar boşsa boş liste döndür
        if (testSuite.getPreconditions() == null || testSuite.getPreconditions().isEmpty()) {
            return results;
        }

        // TODO: Ön koşulları çalıştır
        // Bu kısım, TestRunner'ın adım çalıştırma mantığına benzer şekilde uygulanmalıdır

        return results;
    }

    /**
     * Son koşulları çalıştırır
     * @param testSuite Test Suite
     * @return Son koşul sonuçları
     */
    private List<TestStepResult> runPostconditions(TestSuite testSuite) {
        logger.info("Son koşullar çalıştırılıyor: {} ({})", testSuite.getId(), testSuite.getName());

        List<TestStepResult> results = new ArrayList<>();

        // Son koşullar boşsa boş liste döndür
        if (testSuite.getPostconditions() == null || testSuite.getPostconditions().isEmpty()) {
            return results;
        }

        // TODO: Son koşulları çalıştır
        // Bu kısım, TestRunner'ın adım çalıştırma mantığına benzer şekilde uygulanmalıdır

        return results;
    }

    /**
     * Testleri çalıştırır
     * @param testSuite Test Suite
     * @param result Test Suite sonucu
     * @return Başarı durumu
     */
    private boolean runTests(TestSuite testSuite, TestSuiteResult result) {
        logger.info("Testler çalıştırılıyor: {} ({})", testSuite.getId(), testSuite.getName());

        // Testler boşsa başarılı döndür
        if (testSuite.getTests() == null || testSuite.getTests().isEmpty()) {
            logger.warn("Test Suite'te test bulunamadı: {} ({})", testSuite.getId(), testSuite.getName());
            return true;
        }

        // Çalıştırma stratejisine göre testleri çalıştır
        if (testSuite.getExecutionStrategy().getType() == TestSuite.TestSuiteExecutionStrategy.ExecutionType.SEQUENTIAL) {
            return runTestsSequentially(testSuite, result);
        } else {
            return runTestsInParallel(testSuite, result);
        }
    }

    /**
     * Testleri sıralı olarak çalıştırır
     * @param testSuite Test Suite
     * @param result Test Suite sonucu
     * @return Başarı durumu
     */
    private boolean runTestsSequentially(TestSuite testSuite, TestSuiteResult result) {
        logger.info("Testler sıralı olarak çalıştırılıyor: {} ({})", testSuite.getId(), testSuite.getName());

        boolean allTestsSuccessful = true;

        for (Test test : testSuite.getTests()) {
            // Test yapılandırmasını Test Suite yapılandırmasıyla birleştir
            mergeTestConfiguration(test, testSuite.getConfiguration());

            try {
                // Testi çalıştır
                CompletableFuture<TestResult> future = testService.runTestAuto(test.getId());
                TestResult testResult = future.join(); // Senkron olarak bekle

                // Test sonucunu Test Suite sonucuna ekle
                result.getTestResults().add(testResult);
                result = testSuiteResultRepository.save(result);

                // WebSocket ile sonucu bildir
                webSocketService.sendTestSuiteResult(result);

                // Test başarısız olduysa ve ilk hatada durma seçeneği etkinse döngüden çık
                if (testResult.getStatus() == TestStatus.FAILED &&
                    testSuite.getExecutionStrategy().isStopOnFirstFailure()) {
                    logger.info("Test başarısız oldu ve ilk hatada durma seçeneği etkin, diğer testler atlanıyor");
                    allTestsSuccessful = false;
                    break;
                }

                // Test başarısız olduysa başarı durumunu güncelle
                if (testResult.getStatus() != TestStatus.COMPLETED) {
                    allTestsSuccessful = false;
                }
            } catch (Exception e) {
                logger.error("Test çalıştırılırken hata oluştu: {} ({})", test.getId(), test.getName(), e);
                allTestsSuccessful = false;

                // Test Suite'in ilk hatada durma seçeneği etkinse döngüden çık
                if (testSuite.getExecutionStrategy().isStopOnFirstFailure()) {
                    break;
                }
            }
        }

        return allTestsSuccessful;
    }

    /**
     * Testleri paralel olarak çalıştırır
     * @param testSuite Test Suite
     * @param result Test Suite sonucu
     * @return Başarı durumu
     */
    private boolean runTestsInParallel(TestSuite testSuite, TestSuiteResult result) {
        logger.info("Testler paralel olarak çalıştırılıyor: {} ({})", testSuite.getId(), testSuite.getName());

        int maxParallelTests = testSuite.getExecutionStrategy().getMaxParallelTests();
        ExecutorService executor = Executors.newFixedThreadPool(maxParallelTests);

        try {
            // Tüm testler için CompletableFuture listesi oluştur
            List<CompletableFuture<TestResult>> futures = testSuite.getTests().stream()
                    .map(test -> {
                        // Test yapılandırmasını Test Suite yapılandırmasıyla birleştir
                        mergeTestConfiguration(test, testSuite.getConfiguration());

                        // Testi çalıştır
                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                CompletableFuture<TestResult> future = testService.runTestAuto(test.getId());
                                return future.join();
                            } catch (Exception e) {
                                logger.error("Test çalıştırılırken hata oluştu: {} ({})", test.getId(), test.getName(), e);
                                throw new RuntimeException(e);
                            }
                        }, executor);
                    })
                    .collect(Collectors.toList());

            // Tüm testlerin tamamlanmasını bekle
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            // Tüm testler tamamlandığında sonuçları topla
            allFutures.thenAccept(v -> {
                List<TestResult> testResults = futures.stream()
                        .map(future -> {
                            try {
                                return future.join();
                            } catch (Exception e) {
                                logger.error("Test sonucu alınırken hata oluştu", e);
                                return null;
                            }
                        })
                        .filter(testResult -> testResult != null)
                        .collect(Collectors.toList());

                // Test sonuçlarını Test Suite sonucuna ekle
                result.getTestResults().addAll(testResults);
                testSuiteResultRepository.save(result);

                // WebSocket ile sonucu bildir
                webSocketService.sendTestSuiteResult(result);
            }).join();

            // Başarı durumunu kontrol et
            boolean allTestsSuccessful = result.getTestResults().stream()
                    .allMatch(testResult -> testResult.getStatus() == TestStatus.COMPLETED);

            return allTestsSuccessful;
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Test yapılandırmasını Test Suite yapılandırmasıyla birleştirir
     * @param test Test
     * @param suiteConfig Test Suite yapılandırması
     */
    private void mergeTestConfiguration(Test test, TestSuite.TestSuiteConfiguration suiteConfig) {
        // Test'in kendi yapılandırması yoksa Test Suite yapılandırmasını kullan
        if (test.getBrowserOptions() == null) {
            test.setBrowserPreference(suiteConfig.getBrowserPreference());
            test.setHeadless(suiteConfig.isHeadless());
            test.setTakeScreenshots(suiteConfig.isTakeScreenshots());
            test.setBrowserFullScreen(suiteConfig.isBrowserFullScreen());
        }

        // Test'in kendi continueOnFailure değeri yoksa Test Suite değerini kullan
        if (test.isContinueOnFailure() != suiteConfig.isContinueOnFailure()) {
            test.setContinueOnFailure(suiteConfig.isContinueOnFailure());
        }

        // Test'in kendi stepOptions değeri yoksa Test Suite değerini kullan
        if (suiteConfig.getStepOptions() != null) {
            TestStepOptions testStepOptions = test.getStepOptions();
            if (testStepOptions == null) {
                test.setStepOptions(suiteConfig.getStepOptions());
            }
        }
    }
}
