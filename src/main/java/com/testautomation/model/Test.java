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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tests")
public class Test {
    @Id
    private String id;
    private String name;
    private String description;
    @Builder.Default
    private TestStatus status = TestStatus.QUEUED;

    @Builder.Default
    private TestPriority priority = TestPriority.MEDIUM;

    @Builder.Default
    private TestCategory category = TestCategory.FUNCTIONAL;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private String browserPreference = "chromium";

    @Builder.Default
    private boolean headless = true;

    @Builder.Default
    private boolean takeScreenshots = true;

    @Builder.Default
    private boolean browserFullScreen = true;

    /**
     * Browser options for more detailed configuration
     */
    private BrowserOptions browserOptions;

    @Builder.Default
    private List<TestStep> steps = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime queuedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String agentId;
    private Object results;
    private String error;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    private String preconditions;
    private String expectedResults;

    /**
     * Test context for variables and artifacts
     */
    private TestContext testContext;

    /**
     * Base URL for the test
     */
    private String baseUrl;

    /**
     * Whether to continue test execution if a step fails
     */
    @Builder.Default
    private boolean continueOnFailure = false;

    /**
     * Maximum number of retries for the entire test
     */
    @Builder.Default
    private int maxRetries = 0;

    /**
     * Step options for all steps in the test
     */
    private TestStepOptions stepOptions;

    /**
     * Get browser options, creating default options if none exist
     */
    public BrowserOptions getBrowserOptions() {
        if (browserOptions == null) {
            browserOptions = BrowserOptions.builder()
                .browserType(browserPreference)
                .headless(headless)
                .fullScreen(browserFullScreen)
                .build();
        }
        return browserOptions;
    }

    /**
     * Get test context, creating default context if none exists
     */
    public TestContext getTestContext() {
        if (testContext == null) {
            testContext = TestContext.builder().build();
        }
        return testContext;
    }

    /**
     * Get step options, creating default options if none exist
     */
    public TestStepOptions getStepOptions() {
        if (stepOptions == null) {
            stepOptions = TestStepOptions.builder().build();
        }
        return stepOptions;
    }

    /**
     * Set step options
     */
    public void setStepOptions(TestStepOptions stepOptions) {
        this.stepOptions = stepOptions;
    }

    public void updateStatus(TestStatus status, Map<String, Object> data) {
        this.status = status;

        switch (status) {
            case QUEUED:
                this.queuedAt = LocalDateTime.now();
                break;
            case RUNNING:
                this.startedAt = LocalDateTime.now();
                break;
            case COMPLETED:
            case FAILED:
            case TIMEOUT:
            case CANCELLED:
                this.completedAt = LocalDateTime.now();
                if (data != null && data.containsKey("error")) {
                    this.error = (String) data.get("error");
                }
                if (data != null && data.containsKey("results")) {
                    this.results = data.get("results");
                }
                break;
        }
    }
}
