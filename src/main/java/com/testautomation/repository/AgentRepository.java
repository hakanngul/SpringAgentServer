package com.testautomation.repository;

import com.testautomation.model.Agent;
import com.testautomation.model.AgentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends MongoRepository<Agent, String> {
    List<Agent> findByStatus(AgentStatus status);
}