package com.testautomation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single step in a test scenario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStep {
    /**
     * The action to perform (e.g., click, fill, navigate)
     */
    private String action;

    /**
     * The TestAction enum value corresponding to the action string
     */
    @Builder.Default
    private TestAction actionType = null;

    /**
     * CSS selector, XPath, etc. for targeting elements
     */
    private String target;

    /**
     * Selector strategy (ID, CLASS, NAME, XPATH, CSS)
     */
    private String strategy;

    /**
     * Value to fill, text to check, etc.
     */
    private String value;

    /**
     * Additional options for the action as a map
     */
    @Builder.Default
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Structured options for the action
     */
    private TestStepOptions stepOptions;

    /**
     * Timeout in milliseconds
     */
    @Builder.Default
    private int timeout = 30000;

    /**
     * Human-readable description of the step
     */
    private String description;

    /**
     * Get the action type as an enum
     */
    public TestAction getActionType() {
        if (actionType == null && action != null) {
            try {
                actionType = TestAction.fromString(action);
            } catch (IllegalArgumentException e) {
                // Keep actionType as null if the action string is not recognized
            }
        }
        return actionType;
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
}
