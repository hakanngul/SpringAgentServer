package com.testautomation.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableMongoRepositories(basePackages = "com.testautomation.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        return Collections.singleton("com.testautomation.model");
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), getDatabaseName());
        
        // Ensure collections exist
        if (!mongoTemplate.collectionExists("tests")) {
            mongoTemplate.createCollection("tests");
        }
        if (!mongoTemplate.collectionExists("test_results")) {
            mongoTemplate.createCollection("test_results");
        }
        if (!mongoTemplate.collectionExists("logs")) {
            mongoTemplate.createCollection("logs");
        }
        if (!mongoTemplate.collectionExists("agents")) {
            mongoTemplate.createCollection("agents");
        }
        
        // Create indexes if needed
        // Note: This is usually handled by @Indexed annotations on entity classes
        // But you can also create them programmatically here if needed
    }
}