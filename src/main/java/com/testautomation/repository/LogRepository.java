package com.testautomation.repository;

import com.testautomation.model.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<LogEntry, String> {
    List<LogEntry> findByAgentId(String agentId);
    List<LogEntry> findByTestId(String testId);
}
