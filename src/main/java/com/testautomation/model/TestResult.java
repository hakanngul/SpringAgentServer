package com.testautomation.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "test_results")
public class TestResult {
    @Id
    private String id;
    private String testId;
    private String agentId;
    private String name;
    private String description;
    private String browserPreference;
    private boolean headless;
    private boolean takeScreenshots;
    private boolean browserFullScreen;
    private boolean success;
    private String startTime;
    private String endTime;
    private long duration;
    private List<TestStepResult> steps = new ArrayList<>();
    private List<LogEntry> logs = new ArrayList<>();
    private List<String> screenshots = new ArrayList<>();
}
