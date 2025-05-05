package com.testautomation.model;

import com.testautomation.model.enums.AgentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentStatusInfo {
    private String id;
    private String type;
    private AgentStatus status;
    private LocalDateTime lastActivity;
    private LocalDateTime createdAt;
    
    public AgentStatusInfo(Agent agent) {
        this.id = agent.getId();
        this.type = agent.getType();
        this.status = agent.getStatus();
        this.lastActivity = agent.getLastActivity();
        this.createdAt = agent.getCreatedAt();
    }
}