package com.testautomation.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TestStep {
    private String action;
    private String target;
    private String strategy;
    private String value;
    private String description;
    private Map<String, Object> additionalProperties = new HashMap<>();
}
