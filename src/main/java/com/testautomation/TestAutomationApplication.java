package com.testautomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoRepositories
@EnableAsync
@EnableScheduling
public class TestAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestAutomationApplication.class, args);
    }
}
