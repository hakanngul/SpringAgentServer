package com.testautomation.model;

import com.testautomation.model.enums.TestCategory;
import com.testautomation.model.enums.TestPriority;
import com.testautomation.model.enums.TestStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "tests")
public class Test {
    @Id
    private String id;
    private String name;
    private String description;
    private TestStatus status = TestStatus.QUEUED;
    private TestPriority priority = TestPriority.MEDIUM;
    private TestCategory category = TestCategory.FUNCTIONAL;
    private List<String> tags = new ArrayList<>();
    private String browserPreference = "chromium";
    private boolean headless = true;
    private boolean takeScreenshots = true;
    private boolean browserFullScreen = true;
    private List<TestStep> steps = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime queuedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String agentId;
    private Object results;
    private String error;
    private Map<String, Object> metadata = new HashMap<>();
    private String preconditions;
    private String expectedResults;
    
    public Test() {
        this.createdAt = LocalDateTime.now();
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
