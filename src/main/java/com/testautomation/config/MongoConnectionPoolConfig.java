package com.testautomation.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB bağlantı havuzu yapılandırması
 * Bu sınıf, MongoDB bağlantı havuzunun özelleştirilmiş ayarlarını yapılandırır.
 */
@Configuration
public class MongoConnectionPoolConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${spring.data.mongodb.connection-pool-size:20}")
    private int connectionPoolSize;

    @Value("${spring.data.mongodb.connection-pool-max-wait-time:5000}")
    private int maxWaitTime;

    @Value("${spring.data.mongodb.connection-pool-max-idle-time:60000}")
    private int maxIdleTime;

    @Value("${spring.data.mongodb.socket-timeout:5000}")
    private int socketTimeout;

    @Value("${spring.data.mongodb.connect-timeout:5000}")
    private int connectTimeout;

    /**
     * MongoDB istemci ayarlarını yapılandırır
     * @return Özelleştirilmiş MongoClientSettings
     */
    @Bean
    public MongoClientSettings mongoClientSettings() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        
        ConnectionPoolSettings connectionPoolSettings = ConnectionPoolSettings.builder()
                .maxSize(connectionPoolSize)
                .maxWaitTime(maxWaitTime, TimeUnit.MILLISECONDS)
                .maxConnectionIdleTime(maxIdleTime, TimeUnit.MILLISECONDS)
                .build();
                
        return MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(builder -> builder.applySettings(connectionPoolSettings))
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                           .readTimeout(socketTimeout, TimeUnit.MILLISECONDS))
                .build();
    }
}
