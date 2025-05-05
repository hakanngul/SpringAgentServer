package com.testautomation.service.core;

import com.testautomation.model.Agent;
import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.model.enums.AgentStatus;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.repository.AgentRepository;
import com.testautomation.repository.TestRepository;
import com.testautomation.repository.TestResultRepository;
import com.testautomation.service.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentService {
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final WebSocketService webSocketService;
    private final AgentRepository agentRepository;

    public String registerAgent() {
        String agentId = UUID.randomUUID().toString();

        // Yeni agent oluştur ve veritabanına kaydet
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setStatus(AgentStatus.IDLE);
        agent.setLastActivity(java.time.LocalDateTime.now());
        agent.setCreatedAt(java.time.LocalDateTime.now());
        agent.setUpdatedAt(java.time.LocalDateTime.now());

        try {
            agentRepository.save(agent);
            logger.info("Agent kaydedildi: {}", agentId);
        } catch (Exception e) {
            logger.error("Agent kaydedilirken hata oluştu: {}", e.getMessage());
        }

        webSocketService.sendAgentStatus(agentId, "REGISTERED");
        return agentId;
    }

    public void heartbeat(String agentId) {
        // Agent durumunu güncelle
        try {
            Agent agent = agentRepository.findById(agentId).orElse(null);
            if (agent != null) {
                agent.setLastActivity(java.time.LocalDateTime.now());
                agent.setUpdatedAt(java.time.LocalDateTime.now());
                agentRepository.save(agent);
                logger.debug("Agent heartbeat güncellendi: {}", agentId);
            }
        } catch (Exception e) {
            logger.error("Agent heartbeat güncellenirken hata oluştu: {}", e.getMessage());
        }

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

        // Agent durumunu güncelle
        try {
            Agent agent = agentRepository.findById(agentId).orElse(null);
            if (agent != null) {
                agent.setStatus(AgentStatus.OFFLINE);
                agent.setUpdatedAt(java.time.LocalDateTime.now());
                agentRepository.save(agent);
                logger.info("Agent durumu güncellendi: {} (OFFLINE)", agentId);
            }
        } catch (Exception e) {
            logger.error("Agent durumu güncellenirken hata oluştu: {}", e.getMessage());
        }

        webSocketService.sendAgentStatus(agentId, "DEREGISTERED");
    }

    /**
     * Agent durumunu güncelle
     * @param agentId Agent ID
     * @param status Yeni durum
     */
    public void updateAgentStatus(String agentId, AgentStatus status) {
        try {
            Agent agent = agentRepository.findById(agentId).orElse(null);
            if (agent != null) {
                agent.setStatus(status);
                agent.setLastActivity(java.time.LocalDateTime.now());
                agent.setUpdatedAt(java.time.LocalDateTime.now());
                agentRepository.save(agent);
                logger.info("Agent durumu güncellendi: {} ({})", agentId, status);

                // WebSocket ile durumu bildir
                String statusStr = status.toString();
                webSocketService.sendAgentStatus(agentId, statusStr);
            } else {
                logger.warn("Agent bulunamadı: {}", agentId);
            }
        } catch (Exception e) {
            logger.error("Agent durumu güncellenirken hata oluştu: {}", e.getMessage());
        }
    }

    public List<Test> getAgentTests(String agentId) {
        return testRepository.findByAgentId(agentId);
    }

    public List<TestResult> getAgentTestResults(String agentId) {
        return testResultRepository.findByAgentId(agentId);
    }
}
