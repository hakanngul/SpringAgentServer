package com.testautomation.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "agents")
public class Agent {
    @Id
    private String id;
    private String type;
    private AgentStatus status;
    private LocalDateTime lastActivity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public void updateStatus(AgentStatus newStatus) {
        this.status = newStatus;
        this.lastActivity = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}