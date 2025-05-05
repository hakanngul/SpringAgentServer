package com.testautomation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    public String screenshotsDir() {
        return "test-screenshots";
    }
    
    @Bean
    public SimpMessagingTemplate messagingTemplate() {
        return mock(SimpMessagingTemplate.class);
    }
}
