package com.testautomation.service.runners;

import com.microsoft.playwright.Page;
import com.testautomation.model.TestStep;
import com.testautomation.model.TestStepResult;
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
        Map<String, Object> dataSet
    ) throws Exception {
        boolean allStepsSuccessful = true;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        
        for (int i = 0; i < steps.size(); i++) {
            TestStep step = steps.get(i);
            TestStepResult stepResult = new TestStepResult();
            
            stepResult.setIndex(i);
            stepResult.setAction(step.getAction());
            stepResult.setDescription(step.getDescription());
            stepResult.setStartTime(LocalDateTime.now().format(formatter));
            
            long startTime = System.currentTimeMillis();
            
            try {
                logFn.accept("INFO", "Executing step " + (i + 1) + "/" + steps.size() + ": " + step.getDescription());
                
                StepExecutor.executeStep(page, step, i, steps.size(), variables, dataSet);
                
                stepResult.setSuccess(true);
                
                if (takeScreenshots) {
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
                
                logFn.accept("INFO", "Step " + (i + 1) + " completed successfully");
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
                
                break;
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
