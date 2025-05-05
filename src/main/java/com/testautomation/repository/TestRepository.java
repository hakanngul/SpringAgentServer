package com.testautomation.repository;

import com.testautomation.model.Test;
import com.testautomation.model.enums.TestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends MongoRepository<Test, String> {
    List<Test> findByStatus(TestStatus status);
    List<Test> findByAgentId(String agentId);
}
