package com.testautomation.service.websocket;

import com.testautomation.model.LogEntry;
import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.service.core.AutoScalerService.AutoScalerOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

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

    /**
     * Otomatik ölçeklendirme durumunu gönder
     * @param options Otomatik ölçeklendirme seçenekleri
     */
    public void sendAutoScalerStatus(AutoScalerOptions options) {
        messagingTemplate.convertAndSend("/topic/autoscaler/status", options);
    }

    /**
     * Otomatik ölçeklendirme olayını gönder
     * @param eventType Olay tipi
     * @param data Olay verileri
     */
    public void sendAutoScalerEvent(String eventType, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/topic/autoscaler/events",
            new AutoScalerEventMessage(eventType, data));
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

    @Getter
    private static class AutoScalerEventMessage {
        private final String eventType;
        private final Map<String, Object> data;

        public AutoScalerEventMessage(String eventType, Map<String, Object> data) {
            this.eventType = eventType;
            this.data = data;
        }
    }
}
