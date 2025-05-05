package com.testautomation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to run a test
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {
    private Test test;                // The test to run
    private boolean async;            // Whether to run the test asynchronously
    private String callbackUrl;       // URL to call when test completes (for async tests)
    private String agentId;           // ID of the agent to run the test on
}
