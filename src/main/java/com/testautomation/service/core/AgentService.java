package com.testautomation.service.core;

import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.repository.TestRepository;
import com.testautomation.repository.TestResultRepository;
import com.testautomation.service.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentService {
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final WebSocketService webSocketService;
    
    public String registerAgent() {
        String agentId = UUID.randomUUID().toString();
        webSocketService.sendAgentStatus(agentId, "REGISTERED");
        return agentId;
    }
    
    public void heartbeat(String agentId) {
        webSocketService.sendAgentStatus(agentId, "ACTIVE");
    }
    
    public void deregisterAgent(String agentId) {
        // Cancel any running tests for this agent
        List<Test> runningTests = testRepository.findByAgentId(agentId);
        
        for (Test test : runningTests) {
            if (test.getStatus() == TestStatus.RUNNING || test.getStatus() == TestStatus.QUEUED) {
                Map<String, Object> data = new HashMap<>();
                data.put("error", "Agent disconnected");
                test.updateStatus(TestStatus.CANCELLED, data);
                testRepository.save(test);
                webSocketService.sendTestStatus(test);
            }
        }
        
        webSocketService.sendAgentStatus(agentId, "DEREGISTERED");
    }
    
    public List<Test> getAgentTests(String agentId) {
        return testRepository.findByAgentId(agentId);
    }
    
    public List<TestResult> getAgentTestResults(String agentId) {
        return testResultRepository.findByAgentId(agentId);
    }
}
