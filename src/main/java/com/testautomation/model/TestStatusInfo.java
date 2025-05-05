package com.testautomation.model;

import com.testautomation.model.enums.TestCategory;
import com.testautomation.model.enums.TestPriority;
import com.testautomation.model.enums.TestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestStatusInfo {
    private String id;
    private String name;
    private TestStatus status;
    private TestPriority priority;
    private TestCategory category;
    private LocalDateTime queuedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String agentId;
    private String error;
    
    public TestStatusInfo(Test test) {
        this.id = test.getId();
        this.name = test.getName();
        this.status = test.getStatus();
        this.priority = test.getPriority();
        this.category = test.getCategory();
        this.queuedAt = test.getQueuedAt();
        this.startedAt = test.getStartedAt();
        this.completedAt = test.getCompletedAt();
        this.agentId = test.getAgentId();
        this.error = test.getError();
    }
}