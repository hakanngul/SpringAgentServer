package com.testautomation.controller;

import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.service.core.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {
    private final AgentService agentService;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerAgent() {
        String agentId = agentService.registerAgent();
        return ResponseEntity.ok(Map.of("agentId", agentId));
    }
    
    @PostMapping("/{agentId}/heartbeat")
    public ResponseEntity<Void> heartbeat(@PathVariable String agentId) {
        agentService.heartbeat(agentId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{agentId}/deregister")
    public ResponseEntity<Void> deregisterAgent(@PathVariable String agentId) {
        agentService.deregisterAgent(agentId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{agentId}/tests")
    public ResponseEntity<List<Test>> getAgentTests(@PathVariable String agentId) {
        List<Test> tests = agentService.getAgentTests(agentId);
        return ResponseEntity.ok(tests);
    }
    
    @GetMapping("/{agentId}/results")
    public ResponseEntity<List<TestResult>> getAgentTestResults(@PathVariable String agentId) {
        List<TestResult> results = agentService.getAgentTestResults(agentId);
        return ResponseEntity.ok(results);
    }
}
