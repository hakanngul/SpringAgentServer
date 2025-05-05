package com.testautomation.service.runners;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.testautomation.model.*;
import com.testautomation.model.enums.AgentStatus;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.repository.TestRepository;
import com.testautomation.repository.TestResultRepository;
import com.testautomation.service.core.AgentService;
import com.testautomation.service.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final AgentService agentService;

    @Autowired
    @Qualifier("screenshotsDir")
    private String screenshotsDir;

    public CompletableFuture<TestResult> runTest(Test test, String agentId) {
        System.out.println("Starting test: " + test.getName() + " with agent: " + agentId);

        // Agent durumunu BUSY olarak güncelle
        agentService.updateAgentStatus(agentId, AgentStatus.BUSY);

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
                System.out.println("Playwright created successfully");
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
                System.out.println("Launching browser: " + options.getBrowserType() + ", headless: " + options.isHeadless());
                BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(options.isHeadless());

                // Tam ekran modu için ek argümanlar ekle
                if (test.isBrowserFullScreen() && !options.isHeadless()) {
                    // Chromium için
                    if (options.getBrowserType().equalsIgnoreCase("chromium")) {
                        launchOptions.setArgs(List.of(
                            "--start-maximized",
                            "--window-size=1920,1080",
                            "--window-position=0,0",
                            "--disable-infobars",
                            "--no-default-browser-check"
                        ));
                    }
                    // Firefox için
                    else if (options.getBrowserType().equalsIgnoreCase("firefox")) {
                        launchOptions.setArgs(List.of(
                            "--kiosk",
                            "--width=1920",
                            "--height=1080"
                        ));
                    }
                    // Webkit için
                    else if (options.getBrowserType().equalsIgnoreCase("webkit")) {
                        // Webkit için özel argümanlar
                        launchOptions.setArgs(List.of(
                            "--window-size=1920,1080"
                        ));
                    }
                }

                try {
                    browser = browserType.launch(launchOptions);
                    System.out.println("Browser launched successfully");
                } catch (Exception e) {
                    System.err.println("Failed to launch browser: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }

                // Create context
                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setIgnoreHTTPSErrors(options.isIgnoreHttpsErrors());

                // Tam ekran modu için hazırlık
                if (test.isBrowserFullScreen()) {
                    try {
                        // Varsayılan geniş ekran boyutu
                        contextOptions.setViewportSize(1920, 1080);
                        logFn.accept("INFO", "Tam ekran modu için viewport boyutu ayarlandı: 1920x1080");
                    } catch (Exception e) {
                        logFn.accept("WARN", "Viewport boyutu ayarlanamadı: " + e.getMessage());
                    }
                } else {
                    // Tam ekran değilse, belirtilen viewport boyutunu kullan
                    contextOptions.setViewportSize(options.getViewportWidth(), options.getViewportHeight());
                }

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
                } else {
                    // En azından bir sayfa yükle
                    page.navigate("about:blank");
                }

                // Tam ekran modu için ek adımlar
                if (test.isBrowserFullScreen()) {
                    try {
                        logFn.accept("INFO", "Tarayıcı penceresi tam ekran yapılıyor");

                        // JavaScript ile pencereyi maksimize et ve tam ekran modunu koru
                        page.evaluate("() => { " +
                            "if (window.screen) {" +
                            "   try {" +
                            "       window.moveTo(0, 0);" +
                            "       window.resizeTo(window.screen.availWidth, window.screen.availHeight);" +
                            "       " +
                            "       // Tam ekran modundan çıkılmasını önlemek için event listener ekle" +
                            "       document.addEventListener('fullscreenchange', function() {" +
                            "           if (!document.fullscreenElement) {" +
                            "               try {" +
                            "                   document.documentElement.requestFullscreen();" +
                            "               } catch(e) { console.error('Fullscreen re-enable failed:', e); }" +
                            "           }" +
                            "       });" +
                            "       " +
                            "       document.addEventListener('webkitfullscreenchange', function() {" +
                            "           if (!document.webkitFullscreenElement) {" +
                            "               try {" +
                            "                   document.documentElement.webkitRequestFullscreen();" +
                            "               } catch(e) { console.error('Webkit fullscreen re-enable failed:', e); }" +
                            "           }" +
                            "       });" +
                            "   } catch (e) { console.error('Window resize failed:', e); }" +
                            "}" +
                        "}");

                        // JavaScript ile tam ekran modunu etkinleştir
                        try {
                            page.evaluate("() => {" +
                                "try {" +
                                "   if (document.documentElement.requestFullscreen) {" +
                                "       document.documentElement.requestFullscreen();" +
                                "   } else if (document.documentElement.webkitRequestFullscreen) {" +
                                "       document.documentElement.webkitRequestFullscreen();" +
                                "   } else if (document.documentElement.msRequestFullscreen) {" +
                                "       document.documentElement.msRequestFullscreen();" +
                                "   } else if (document.documentElement.mozRequestFullScreen) {" +
                                "       document.documentElement.mozRequestFullScreen();" +
                                "   }" +
                                "} catch(e) { console.error('Fullscreen request failed:', e); }" +
                            "}");

                            // Kısa bir bekleme ekle
                            Thread.sleep(500);

                            // Klavye kısayolu ile F11 tuşuna basma (tam ekran)
                            page.keyboard().press("F11");

                            logFn.accept("INFO", "JavaScript ve F11 tuşu ile tam ekran denendi");
                        } catch (Exception e) {
                            logFn.accept("WARN", "Tam ekran yapılamadı: " + e.getMessage());
                        }

                        logFn.accept("INFO", "Tarayıcı penceresi tam ekran yapıldı");

                        // Tam ekran modunun etkin olup olmadığını kontrol et ve gerekirse tekrar dene
                        try {
                            // Kısa bir bekleme ekle
                            Thread.sleep(1000);

                            // Tam ekran modunun etkin olup olmadığını kontrol et
                            Boolean isFullScreen = (Boolean) page.evaluate("() => {" +
                                "return !!(document.fullscreenElement || document.webkitFullscreenElement || " +
                                "document.mozFullScreenElement || document.msFullscreenElement);" +
                            "}");

                            if (isFullScreen != null && !isFullScreen) {
                                logFn.accept("INFO", "Tam ekran modu etkin değil, tekrar deneniyor...");

                                // Tam ekran modunu tekrar etkinleştir
                                page.evaluate("() => {" +
                                    "try {" +
                                    "   if (document.documentElement.requestFullscreen) {" +
                                    "       document.documentElement.requestFullscreen();" +
                                    "   } else if (document.documentElement.webkitRequestFullscreen) {" +
                                    "       document.documentElement.webkitRequestFullscreen();" +
                                    "   } else if (document.documentElement.msRequestFullscreen) {" +
                                    "       document.documentElement.msRequestFullscreen();" +
                                    "   } else if (document.documentElement.mozRequestFullScreen) {" +
                                    "       document.documentElement.mozRequestFullScreen();" +
                                    "   }" +
                                    "} catch(e) { console.error('Fullscreen retry failed:', e); }" +
                                "}");

                                // F11 tuşuna tekrar bas
                                page.keyboard().press("F11");
                            }
                        } catch (Exception e) {
                            logFn.accept("WARN", "Tam ekran modu kontrolü başarısız: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        logFn.accept("WARN", "Tarayıcı penceresi tam ekran yapılamadı: " + e.getMessage());
                    }
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

                // Adım olmayan testler için başarılı kabul et
                if (test.getSteps() == null || test.getSteps().isEmpty()) {
                    success = true;
                    logFn.accept("INFO", "Test has no steps, marking as successful");
                }

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
                System.err.println("Test execution error: " + e.getMessage());
                e.printStackTrace();
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

                // Test tamamlandığında agent durumunu IDLE olarak güncelle
                agentService.updateAgentStatus(agentId, AgentStatus.IDLE);
            }

            System.out.println("Test completed: " + test.getName() + ", success: " + result.isSuccess());
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
