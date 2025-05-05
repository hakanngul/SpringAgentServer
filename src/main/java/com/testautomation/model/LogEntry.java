package com.testautomation.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "logs")
public class LogEntry {
    @Id
    private String id;
    private String timestamp;
    private String level;
    private String message;
    private String agentId;
    private String testId;
}
