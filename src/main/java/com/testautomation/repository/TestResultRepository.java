package com.testautomation.repository;

import com.testautomation.model.TestResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository extends MongoRepository<TestResult, String> {
    List<TestResult> findByTestId(String testId);
    List<TestResult> findByAgentId(String agentId);
}
