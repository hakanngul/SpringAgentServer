package com.testautomation.service.runners;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.testautomation.model.*;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.repository.TestRepository;
import com.testautomation.repository.TestResultRepository;
import com.testautomation.service.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestRunner {
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final WebSocketService webSocketService;

    @Autowired
    @Qualifier("screenshotsDir")
    private String screenshotsDir;

    public CompletableFuture<TestResult> runTest(Test test, String agentId) {
        return CompletableFuture.supplyAsync(() -> {
            TestResult result = new TestResult();
            result.setId(UUID.randomUUID().toString());
            result.setTestId(test.getId());
            result.setAgentId(agentId);
            result.setName(test.getName());
            result.setDescription(test.getDescription());
            result.setBrowserPreference(test.getBrowserPreference());
            result.setHeadless(test.isHeadless());
            result.setTakeScreenshots(test.isTakeScreenshots());
            result.setBrowserFullScreen(test.isBrowserFullScreen());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            result.setStartTime(LocalDateTime.now().format(formatter));

            long startTime = System.currentTimeMillis();

            // Update test status to RUNNING
            test.updateStatus(TestStatus.RUNNING, null);
            testRepository.save(test);
            webSocketService.sendTestStatus(test);

            // Create log function
            BiConsumer<String, String> logFn = (level, message) -> {
                LogEntry logEntry = new LogEntry();
                logEntry.setId(UUID.randomUUID().toString());
                logEntry.setTimestamp(LocalDateTime.now().format(formatter));
                logEntry.setLevel(level);
                logEntry.setMessage(message);
                logEntry.setAgentId(agentId);
                logEntry.setTestId(test.getId());

                result.getLogs().add(logEntry);
                webSocketService.sendTestLog(test.getId(), logEntry);
            };

            try (Playwright playwright = Playwright.create()) {
                // Get browser options
                BrowserOptions options = test.getBrowserOptions();

                // Select browser
                Browser browser;
                BrowserType browserType;

                switch (options.getBrowserType().toLowerCase()) {
                    case "firefox":
                        browserType = playwright.firefox();
                        break;
                    case "webkit":
                        browserType = playwright.webkit();
                        break;
                    case "chromium":
                    default:
                        browserType = playwright.chromium();
                        break;
                }

                // Launch browser
                BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(options.isHeadless());

                browser = browserType.launch(launchOptions);

                // Create context
                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(options.getViewportWidth(), options.getViewportHeight())
                    .setIgnoreHTTPSErrors(options.isIgnoreHttpsErrors());

                // Kullanıcı ajanını burada ayarla
                if (options.getUserAgent() != null) {
                    contextOptions.setUserAgent(options.getUserAgent());
                }

                BrowserContext context = browser.newContext(contextOptions);

                // Create page
                Page page = context.newPage();

                // Navigate to base URL if specified
                if (test.getBaseUrl() != null && !test.getBaseUrl().isEmpty()) {
                    page.navigate(test.getBaseUrl());
                    logFn.accept("INFO", "Navigated to base URL: " + test.getBaseUrl());
                }

                // Get test context
                TestContext testContext = test.getTestContext();
                Map<String, Object> variables = testContext.getVariables();
                Map<String, Object> dataSet = test.getMetadata() != null ? test.getMetadata() : new HashMap<>();

                TestStepExecutor executor = new TestStepExecutor(
                    page,
                    test.getName(),
                    screenshotsDir,
                    test.isTakeScreenshots(),
                    logFn
                );

                logFn.accept("INFO", "Starting test execution: " + test.getName());

                boolean success = false;
                int retryCount = 0;
                Exception lastException = null;

                do {
                    try {
                        if (retryCount > 0) {
                            logFn.accept("INFO", "Retrying test execution (attempt " + (retryCount + 1) +
                                " of " + (test.getMaxRetries() + 1) + ")");
                        }

                        success = executor.executeSteps(test.getSteps(), result, variables, dataSet,
                            test.isContinueOnFailure());

                        if (success) {
                            break; // Exit retry loop if successful
                        }
                    } catch (Exception e) {
                        lastException = e;
                        logFn.accept("ERROR", "Test execution error: " + e.getMessage());
                    }

                    retryCount++;

                    if (retryCount <= test.getMaxRetries()) {
                        // Clear previous results before retry
                        result.getSteps().clear();
                        logFn.accept("INFO", "Waiting 3 seconds before retry...");
                        Thread.sleep(3000);
                    }
                } while (retryCount <= test.getMaxRetries());

                result.setSuccess(success);

                if (success) {
                    logFn.accept("INFO", "Test completed successfully");
                    test.updateStatus(TestStatus.COMPLETED, Map.of("results", result));
                } else {
                    logFn.accept("ERROR", "Test failed");
                    test.updateStatus(TestStatus.FAILED, Map.of(
                        "error", "Test execution failed",
                        "results", result
                    ));
                }

                // Take final screenshot
                if (test.isTakeScreenshots()) {
                    String screenshotPath = ScreenshotUtils.takeFullPageScreenshot(
                        page,
                        test.getName(),
                        success ? "COMPLETED" : "FAILED",
                        screenshotsDir
                    );
                    result.getScreenshots().add(screenshotPath);
                }

                // Close browser
                context.close();
                browser.close();
            } catch (Exception e) {
                logFn.accept("ERROR", "Test execution error: " + e.getMessage());
                result.setSuccess(false);

                test.updateStatus(TestStatus.FAILED, Map.of(
                    "error", e.getMessage(),
                    "results", result
                ));
            } finally {
                long endTime = System.currentTimeMillis();
                result.setEndTime(LocalDateTime.now().format(formatter));
                result.setDuration(endTime - startTime);

                // Save test result
                testResultRepository.save(result);

                // Update test status
                testRepository.save(test);
                webSocketService.sendTestStatus(test);
                webSocketService.sendTestResult(result);
            }

            return result;
        });
    }

    public CompletableFuture<List<TestResult>> runTests(List<Test> tests, String agentId) {
        List<CompletableFuture<TestResult>> futures = tests.stream()
            .map(test -> runTest(test, agentId))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
}
