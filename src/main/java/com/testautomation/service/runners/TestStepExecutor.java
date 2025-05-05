package com.testautomation.service.runners;

import com.microsoft.playwright.Page;
import com.testautomation.model.*;
import com.testautomation.model.TestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
@RequiredArgsConstructor
public class TestStepExecutor {
    private Page page;
    private String testName;
    private String screenshotsDir;
    private boolean takeScreenshots;
    private BiConsumer<String, String> logFn;

    public TestStepExecutor(Page page, String testName, String screenshotsDir, boolean takeScreenshots, BiConsumer<String, String> logFn) {
        this.page = page;
        this.testName = testName;
        this.screenshotsDir = screenshotsDir;
        this.takeScreenshots = takeScreenshots;
        this.logFn = logFn;
    }

    public boolean executeSteps(
        List<TestStep> steps,
        TestResult result,
        Map<String, Object> variables,
        Map<String, Object> dataSet,
        boolean continueOnFailure
    ) throws Exception {
        boolean allStepsSuccessful = true;
        // Eğer adım yoksa, başarılı olarak kabul et
        if (steps == null || steps.isEmpty()) {
            return true;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        for (int i = 0; i < steps.size(); i++) {
            TestStep step = steps.get(i);
            TestStepResult stepResult = new TestStepResult();

            stepResult.setIndex(i);
            stepResult.setAction(step.getAction());
            stepResult.setDescription(step.getDescription());
            stepResult.setStartTime(LocalDateTime.now().format(formatter));

            long startTime = System.currentTimeMillis();
            TestStepOptions options = step.getStepOptions();

            try {
                logFn.accept("INFO", "Executing step " + (i + 1) + "/" + steps.size() + ": " + step.getDescription());

                // Add delay if specified
                if (options.getDelay() > 0) {
                    Thread.sleep(options.getDelay());
                }

                // Take screenshot before action if requested
                if (takeScreenshots && options.isScreenshotBefore()) {
                    String screenshotPath = ScreenshotUtils.takeScreenshot(
                        page,
                        testName,
                        i,
                        "BEFORE_" + step.getDescription(),
                        screenshotsDir
                    );
                    result.getScreenshots().add(screenshotPath);
                }

                // Execute step with retries if configured
                boolean stepSuccess = false;
                Exception lastStepException = null;

                for (int retryCount = 0; retryCount <= options.getRetries(); retryCount++) {
                    try {
                        if (retryCount > 0) {
                            logFn.accept("INFO", "Retrying step (attempt " + (retryCount + 1) +
                                " of " + (options.getRetries() + 1) + ")");

                            if (options.getRetryDelay() > 0) {
                                Thread.sleep(options.getRetryDelay());
                            }
                        }

                        StepExecutor.executeStep(page, step, variables, dataSet);
                        stepSuccess = true;
                        break; // Exit retry loop if successful
                    } catch (Exception e) {
                        lastStepException = e;
                        logFn.accept("WARN", "Step execution attempt " + (retryCount + 1) +
                            " failed: " + e.getMessage());
                    }
                }

                if (stepSuccess) {
                    stepResult.setSuccess(true);
                    logFn.accept("INFO", "Step " + (i + 1) + " completed successfully");
                } else {
                    throw lastStepException; // Re-throw the last exception to be caught below
                }

                // Take screenshot after action if requested
                if (takeScreenshots && (options.isScreenshotAfter() || step.getAction().equalsIgnoreCase("screenshot"))) {
                    String screenshotPath = ScreenshotUtils.takeScreenshot(
                        page,
                        testName,
                        i,
                        step.getDescription(),
                        screenshotsDir
                    );
                    stepResult.setScreenshot(screenshotPath);
                    result.getScreenshots().add(screenshotPath);
                }
            } catch (Exception e) {
                stepResult.setSuccess(false);
                stepResult.setError(e.getMessage());
                allStepsSuccessful = false;

                logFn.accept("ERROR", "Step " + (i + 1) + " failed: " + e.getMessage());

                if (takeScreenshots) {
                    String screenshotPath = ScreenshotUtils.takeScreenshot(
                        page,
                        testName,
                        i,
                        "ERROR_" + step.getDescription(),
                        screenshotsDir
                    );
                    stepResult.setScreenshot(screenshotPath);
                    result.getScreenshots().add(screenshotPath);
                }

                // Check if we should continue despite the failure
                boolean shouldContinue = continueOnFailure || options.isContinueOnFailure();
                if (!shouldContinue) {
                    break;
                } else {
                    logFn.accept("WARN", "Continuing test execution despite step failure");
                }
            } finally {
                long endTime = System.currentTimeMillis();
                stepResult.setDuration(endTime - startTime);
                stepResult.setEndTime(LocalDateTime.now().format(formatter));
                result.getSteps().add(stepResult);
            }
        }

        return allStepsSuccessful;
    }
}
