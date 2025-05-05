package com.testautomation.service.core;

import com.testautomation.model.Agent;
import com.testautomation.model.AgentStatus;
import com.testautomation.model.AgentStatusInfo;
import com.testautomation.repository.AgentRepository;
import com.testautomation.service.websocket.WebSocketService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentPoolService {
    private static final Logger logger = LoggerFactory.getLogger(AgentPoolService.class);
    
    private final AgentRepository agentRepository;
    private final WebSocketService webSocketService;
    
    @Value("${app.agent.min-agents}")
    private int minAgents;
    
    @Value("${app.agent.max-agents}")
    private int maxAgents;
    
    @Value("${app.agent.idle-timeout}")
    private long idleTimeout;
    
    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        logger.info("Initializing agent pool with {} minimum agents", minAgents);
        for (int i = 0; i < minAgents; i++) {
            createAgent();
        }
    }
    
    public Agent createAgent() {
        if (agents.size() >= maxAgents) {
            logger.warn("Maximum agent count reached ({})", maxAgents);
            return null;
        }
        
        String agentId = UUID.randomUUID().toString();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setStatus(AgentStatus.IDLE);
        agent.setLastActivity(LocalDateTime.now());
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        
        agents.put(agentId, agent);
        
        try {
            agentRepository.save(agent);
            logger.info("Agent saved to database: {}", agentId);
        } catch (Exception e) {
            logger.error("Error saving agent to database: {}", agentId, e);
        }
        
        webSocketService.sendAgentStatus(agentId, "CREATED");
        logger.info("New agent created: {}", agentId);
        
        return agent;
    }
    
    public boolean removeAgent(String agentId) {
        if (!agents.containsKey(agentId)) {
            return false;
        }
        
        agents.remove(agentId);
        
        try {
            Agent agent = agentRepository.findById(agentId).orElse(null);
            if (agent != null) {
                agent.setStatus(AgentStatus.OFFLINE);
                agent.setUpdatedAt(LocalDateTime.now());
                agentRepository.save(agent);
                logger.info("Agent status updated in database: {} (OFFLINE)", agentId);
            }
        } catch (Exception e) {
            logger.error("Error updating agent status in database: {}", agentId, e);
        }
        
        webSocketService.sendAgentStatus(agentId, "REMOVED");
        logger.info("Agent removed: {}", agentId);
        
        return true;
    }
    
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupIdleAgents() {
        LocalDateTime now = LocalDateTime.now();
        List<String> idleAgents = new ArrayList<>();
        
        // Find idle agents
        for (Map.Entry<String, Agent> entry : agents.entrySet()) {
            String agentId = entry.getKey();
            Agent agent = entry.getValue();
            
            if (agent.getStatus() == AgentStatus.IDLE) {
                long idleTime = java.time.Duration.between(agent.getLastActivity(), now).toMillis();
                
                if (idleTime > idleTimeout) {
                    idleAgents.add(agentId);
                }
            }
        }
        
        // Check minimum agent count
        int activeAgents = agents.size() - idleAgents.size();
        int agentsToRemove = Math.max(0, activeAgents - minAgents);
        
        // Remove excess idle agents
        if (agentsToRemove > 0) {
            logger.info("Removing {} idle agents", agentsToRemove);
            
            for (int i = 0; i < agentsToRemove && !idleAgents.isEmpty(); i++) {
                String agentId = idleAgents.remove(idleAgents.size() - 1);
                removeAgent(agentId);
            }
        }
        
        logger.info("Agents ready for testing...");
    }
    
    public Agent getIdleAgent() {
        // Find an idle agent
        for (Agent agent : agents.values()) {
            if (agent.getStatus() == AgentStatus.IDLE) {
                return agent;
            }
        }
        
        // Create a new agent if maximum not reached
        if (agents.size() < maxAgents) {
            return createAgent();
        }
        
        return null;
    }
    
    public boolean resetAgent(String agentId) {
        if (!agents.containsKey(agentId)) {
            return false;
        }
        
        Agent agent = agents.get(agentId);
        agent.setStatus(AgentStatus.IDLE);
        agent.setLastActivity(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        
        try {
            agentRepository.save(agent);
        } catch (Exception e) {
            logger.error("Error resetting agent in database: {}", agentId, e);
            return false;
        }
        
        webSocketService.sendAgentStatus(agentId, "RESET");
        return true;
    }
    
    public AgentStatusInfo getAgentStatus(String agentId) {
        if (!agents.containsKey(agentId)) {
            return null;
        }
        
        Agent agent = agents.get(agentId);
        return new AgentStatusInfo(agent);
    }
    
    public List<AgentStatusInfo> getAllAgents() {
        return agents.values().stream()
                .map(AgentStatusInfo::new)
                .collect(Collectors.toList());
    }
    
    public PoolStatus getPoolStatus() {
        int idleAgents = 0;
        int busyAgents = 0;
        int offlineAgents = 0;
        int errorAgents = 0;
        
        for (Agent agent : agents.values()) {
            switch (agent.getStatus()) {
                case IDLE:
                    idleAgents++;
                    break;
                case BUSY:
                    busyAgents++;
                    break;
                case OFFLINE:
                    offlineAgents++;
                    break;
                case ERROR:
                    errorAgents++;
                    break;
            }
        }
        
        return new PoolStatus(
                agents.size(),
                idleAgents,
                busyAgents,
                offlineAgents,
                errorAgents,
                maxAgents,
                minAgents
        );
    }
    
    @Getter
    public static class PoolStatus {
        private final int totalAgents;
        private final int idleAgents;
        private final int busyAgents;
        private final int offlineAgents;
        private final int errorAgents;
        private final int maxAgents;
        private final int minAgents;
        
        public PoolStatus(int totalAgents, int idleAgents, int busyAgents, 
                          int offlineAgents, int errorAgents, int maxAgents, int minAgents) {
            this.totalAgents = totalAgents;
            this.idleAgents = idleAgents;
            this.busyAgents = busyAgents;
            this.offlineAgents = offlineAgents;
            this.errorAgents = errorAgents;
            this.maxAgents = maxAgents;
            this.minAgents = minAgents;
        }
    }
}