package com.testautomation.service.websocket;

import com.testautomation.model.LogEntry;
import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    
    public void sendTestStatus(Test test) {
        messagingTemplate.convertAndSend("/topic/tests/" + test.getId() + "/status", test);
        messagingTemplate.convertAndSend("/topic/tests/status", test);
    }
    
    public void sendTestResult(TestResult result) {
        messagingTemplate.convertAndSend("/topic/tests/" + result.getTestId() + "/result", result);
        messagingTemplate.convertAndSend("/topic/tests/results", result);
    }
    
    public void sendTestLog(String testId, LogEntry logEntry) {
        messagingTemplate.convertAndSend("/topic/tests/" + testId + "/logs", logEntry);
    }
    
    public void sendAgentStatus(String agentId, String status) {
        messagingTemplate.convertAndSend("/topic/agents/" + agentId + "/status", status);
        messagingTemplate.convertAndSend("/topic/agents/status", 
            new AgentStatusMessage(agentId, status));
    }
    
    @Getter
    private static class AgentStatusMessage {
        private final String agentId;
        private final String status;
        
        public AgentStatusMessage(String agentId, String status) {
            this.agentId = agentId;
            this.status = status;
        }

    }
}
