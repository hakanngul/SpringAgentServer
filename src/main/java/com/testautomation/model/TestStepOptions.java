package com.testautomation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents options for a test step
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStepOptions {
    
    /**
     * Whether to force the action (e.g., click even if the element is not visible)
     */
    @Builder.Default
    private boolean force = false;
    
    /**
     * Whether to wait for animations to complete before performing the action
     */
    @Builder.Default
    private boolean waitForAnimations = true;
    
    /**
     * Delay in milliseconds before performing the action
     */
    @Builder.Default
    private int delay = 0;
    
    /**
     * Number of times to retry the action if it fails
     */
    @Builder.Default
    private int retries = 0;
    
    /**
     * Delay in milliseconds between retries
     */
    @Builder.Default
    private int retryDelay = 1000;
    
    /**
     * Whether to take a screenshot before performing the action
     */
    @Builder.Default
    private boolean screenshotBefore = false;
    
    /**
     * Whether to take a screenshot after performing the action
     */
    @Builder.Default
    private boolean screenshotAfter = false;
    
    /**
     * Whether to continue test execution if this step fails
     */
    @Builder.Default
    private boolean continueOnFailure = false;
}
