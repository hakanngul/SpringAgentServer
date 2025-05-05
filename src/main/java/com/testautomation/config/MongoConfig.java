package com.testautomation.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MongoDB yapılandırma sınıfı
 * Bu sınıf, MongoDB bağlantısını ve veritabanı yapılandırmasını yönetir.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.testautomation.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Autowired
    private Environment environment;

    @Autowired
    private MongoClientSettings mongoClientSettings;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    public MongoClient mongoClient() {
        logger.info("MongoDB bağlantısı oluşturuluyor: {}", maskConnectionString(mongoUri));
        return MongoClients.create(mongoClientSettings);
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        return Collections.singleton("com.testautomation.model");
    }

    /**
     * Uygulama başlatıldıktan sonra veritabanı koleksiyonlarını ve indekslerini oluşturur
     */
    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {
        logger.info("Veritabanı koleksiyonları ve indeksleri başlatılıyor...");

        String[] activeProfiles = environment.getActiveProfiles();
        logger.info("Aktif profiller: {}", Arrays.toString(activeProfiles));

        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), getDatabaseName());

        // Koleksiyonların varlığını kontrol et ve oluştur
        List<String> collections = List.of("tests", "test_results", "logs", "agents");
        for (String collection : collections) {
            if (!mongoTemplate.collectionExists(collection)) {
                logger.info("Koleksiyon oluşturuluyor: {}", collection);
                mongoTemplate.createCollection(collection);
            }
        }

        // İndeksleri oluştur
        createIndexes(mongoTemplate);

        logger.info("Veritabanı başlatma tamamlandı: {}", databaseName);
    }

    /**
     * Veritabanı indekslerini oluşturur
     * @param mongoTemplate MongoDB şablonu
     */
    private void createIndexes(MongoTemplate mongoTemplate) {
        // Tests koleksiyonu indeksleri
        createIndex(mongoTemplate, "tests", Indexes.ascending("status"), "status_index");
        createIndex(mongoTemplate, "tests", Indexes.ascending("agentId"), "agentId_index");
        createIndex(mongoTemplate, "tests", Indexes.ascending("createdAt"), "createdAt_index");

        // Test results koleksiyonu indeksleri
        createIndex(mongoTemplate, "test_results", Indexes.ascending("testId"), "testId_index");
        createIndex(mongoTemplate, "test_results", Indexes.ascending("agentId"), "agentId_index");
        createIndex(mongoTemplate, "test_results", Indexes.ascending("createdAt"), "createdAt_index");

        // Logs koleksiyonu indeksleri
        createIndex(mongoTemplate, "logs", Indexes.ascending("testId"), "testId_index");
        createIndex(mongoTemplate, "logs", Indexes.ascending("agentId"), "agentId_index");
        createIndex(mongoTemplate, "logs", Indexes.ascending("timestamp"), "timestamp_index");
        createIndex(mongoTemplate, "logs", Indexes.ascending("level"), "level_index");

        // Agents koleksiyonu indeksleri
        createIndex(mongoTemplate, "agents", Indexes.ascending("status"), "status_index");
        createIndex(mongoTemplate, "agents", Indexes.ascending("lastActivity"), "lastActivity_index");
    }

    /**
     * Belirtilen koleksiyonda indeks oluşturur
     * @param mongoTemplate MongoDB şablonu
     * @param collectionName Koleksiyon adı
     * @param index İndeks tanımı
     * @param indexName İndeks adı
     */
    private void createIndex(MongoTemplate mongoTemplate, String collectionName,
                             org.bson.conversions.Bson index, String indexName) {
        try {
            IndexOptions options = new IndexOptions().name(indexName);
            mongoTemplate.getCollection(collectionName).createIndex(index, options);
            logger.debug("İndeks oluşturuldu: {} ({})", indexName, collectionName);
        } catch (Exception e) {
            // İndeks zaten varsa hata vermez
            logger.debug("İndeks zaten var veya oluşturulamadı: {} ({}): {}",
                      indexName, collectionName, e.getMessage());
        }
    }

    /**
     * Bağlantı dizesindeki hassas bilgileri maskeler
     * @param connectionString Bağlantı dizesi
     * @return Maskelenmiş bağlantı dizesi
     */
    private String maskConnectionString(String connectionString) {
        if (connectionString == null) {
            return null;
        }

        // mongodb://username:password@host:port/database
        return connectionString.replaceAll("(mongodb://[^:]+:)[^@]+(@.*)", "$1*****$2");
    }
}