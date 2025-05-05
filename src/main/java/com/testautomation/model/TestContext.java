package com.testautomation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the context for a test, including variables and state
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestContext {
    
    /**
     * Variables that can be used in test steps
     */
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();
    
    /**
     * Storage for test artifacts (e.g., screenshots, downloads)
     */
    @Builder.Default
    private Map<String, Object> artifacts = new HashMap<>();
    
    /**
     * Custom data that can be used by test steps
     */
    @Builder.Default
    private Map<String, Object> customData = new HashMap<>();
    
    /**
     * Get a variable value by name
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Set a variable value
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }
    
    /**
     * Check if a variable exists
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }
    
    /**
     * Store an artifact
     */
    public void storeArtifact(String name, Object artifact) {
        artifacts.put(name, artifact);
    }
    
    /**
     * Get an artifact by name
     */
    public Object getArtifact(String name) {
        return artifacts.get(name);
    }
}
