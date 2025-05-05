package com.testautomation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.File;
import java.util.concurrent.Executor;

/**
 * Uygulama yapılandırma sınıfı
 * Bu sınıf, uygulama genelinde kullanılan yapılandırmaları ve beanları tanımlar.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Value("${spring.task.execution.pool.core-size:5}")
    private int corePoolSize;

    @Value("${spring.task.execution.pool.max-size:10}")
    private int maxPoolSize;

    @Value("${spring.task.execution.pool.queue-capacity:25}")
    private int queueCapacity;

    @Value("${spring.task.scheduling.pool.size:5}")
    private int schedulingPoolSize;

    /**
     * Asenkron görevler için görev yürütücüsü
     * @return Görev yürütücüsü
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        logger.info("Görev yürütücüsü yapılandırılıyor: core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, queueCapacity);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("TestRunner-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        return executor;
    }

    /**
     * Zamanlanmış görevler için görev zamanlayıcısı
     * @return Görev zamanlayıcısı
     */
    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        logger.info("Görev zamanlayıcısı yapılandırılıyor: poolSize={}", schedulingPoolSize);

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulingPoolSize);
        scheduler.setThreadNamePrefix("Scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();

        return scheduler;
    }

    /**
     * Ekran görüntüleri dizini
     * @param configuredDir Yapılandırılmış dizin
     * @return Ekran görüntüleri dizini
     */
    @Bean(name = "screenshotsDir")
    public String screenshotsDir(@Value("${app.screenshots-dir}") String configuredDir) {
        logger.info("Ekran görüntüleri dizini yapılandırılıyor: {}", configuredDir);

        // Dizinin var olduğundan emin ol
        File dir = new File(configuredDir);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                logger.info("Ekran görüntüleri dizini oluşturuldu: {}", configuredDir);
            } else {
                logger.warn("Ekran görüntüleri dizini oluşturulamadı: {}", configuredDir);
            }
        }

        return configuredDir;
    }

    /**
     * Raporlar dizini
     * @param configuredDir Yapılandırılmış dizin
     * @return Raporlar dizini
     */
    @Bean(name = "reportsDir")
    public String reportsDir(@Value("${app.reports-dir}") String configuredDir) {
        logger.info("Raporlar dizini yapılandırılıyor: {}", configuredDir);

        // Dizinin var olduğundan emin ol
        File dir = new File(configuredDir);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                logger.info("Raporlar dizini oluşturuldu: {}", configuredDir);
            } else {
                logger.warn("Raporlar dizini oluşturulamadı: {}", configuredDir);
            }
        }

        return configuredDir;
    }
}
