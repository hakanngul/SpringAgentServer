package com.testautomation.service.core;

import com.testautomation.model.events.AutoScalerEvent;
import com.testautomation.service.websocket.WebSocketService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AutoScalerService
 * Agent havuzunu test kuyruğuna göre otomatik ölçeklendirir
 */
@Service
public class AutoScalerService {
    private static final Logger logger = LoggerFactory.getLogger(AutoScalerService.class);

    private final AgentPoolService agentPoolService;
    private final TestQueueService testQueueService;
    private final WebSocketService webSocketService;
    private final ApplicationEventPublisher eventPublisher;

    @Getter
    @Setter
    private AutoScalerOptions options;

    /**
     * AutoScalerService yapıcı metodu
     * @param agentPoolService Agent havuzu servisi
     * @param testQueueService Test kuyruğu servisi
     * @param webSocketService WebSocket servisi
     * @param eventPublisher Event publisher
     */
    @Autowired
    public AutoScalerService(
            AgentPoolService agentPoolService,
            TestQueueService testQueueService,
            WebSocketService webSocketService,
            ApplicationEventPublisher eventPublisher,
            @Value("${app.autoscaler.enabled:false}") boolean enabled,
            @Value("${app.autoscaler.check-interval:10000}") long checkInterval,
            @Value("${app.agent.min-agents:3}") int minAgents,
            @Value("${app.agent.max-agents:10}") int maxAgents,
            @Value("${app.autoscaler.scale-up-threshold:2}") int scaleUpThreshold,
            @Value("${app.autoscaler.scale-down-threshold:0}") int scaleDownThreshold,
            @Value("${app.autoscaler.scale-up-step:1}") int scaleUpStep,
            @Value("${app.autoscaler.scale-down-step:1}") int scaleDownStep
    ) {
        this.agentPoolService = agentPoolService;
        this.testQueueService = testQueueService;
        this.webSocketService = webSocketService;
        this.eventPublisher = eventPublisher;

        // Varsayılan değerler
        this.options = new AutoScalerOptions();
        this.options.setEnabled(enabled);
        this.options.setCheckInterval(checkInterval);
        this.options.setMinAgents(minAgents);
        this.options.setMaxAgents(maxAgents);
        this.options.setScaleUpThreshold(scaleUpThreshold);
        this.options.setScaleDownThreshold(scaleDownThreshold);
        this.options.setScaleUpStep(scaleUpStep);
        this.options.setScaleDownStep(scaleDownStep);

        logger.info("Auto Scaler Service başlatıldı");
        logger.info("Otomatik ölçeklendirme: {}", options.isEnabled() ? "Etkin" : "Devre dışı");
    }

    /**
     * Servis başlatıldığında çalışır
     */
    @PostConstruct
    public void initialize() {
        logger.info("Auto Scaler Service başlatılıyor...");
        logger.info("Otomatik ölçeklendirme seçenekleri: {}", options);
    }

    /**
     * Servis kapatıldığında çalışır
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Auto Scaler Service kapatılıyor...");
    }

    /**
     * Otomatik ölçeklendirmeyi etkinleştir/devre dışı bırak
     * @param enabled Etkin mi?
     */
    public void setEnabled(boolean enabled) {
        boolean previousState = options.isEnabled();
        options.setEnabled(enabled);

        if (previousState != enabled) {
            logger.info("Otomatik ölçeklendirme: {}", enabled ? "Etkinleştirildi" : "Devre dışı bırakıldı");

            // Durum değişikliği olayını yayınla
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("enabled", enabled);
            eventData.put("timestamp", LocalDateTime.now());

            AutoScalerEvent event = new AutoScalerEvent(
                    this,
                    "autoScalerStatusChanged",
                    eventData
            );

            eventPublisher.publishEvent(event);
            webSocketService.sendAutoScalerStatus(options);
        }
    }

    /**
     * Otomatik ölçeklendirme seçeneklerini güncelle
     * @param newOptions Yeni seçenekler
     */
    public void updateOptions(AutoScalerOptions newOptions) {
        boolean statusChanged = false;

        if (newOptions.isEnabled() != options.isEnabled()) {
            statusChanged = true;
        }

        // Seçenekleri güncelle
        if (newOptions.getMinAgents() > 0) {
            options.setMinAgents(newOptions.getMinAgents());
        }

        if (newOptions.getMaxAgents() > 0) {
            options.setMaxAgents(newOptions.getMaxAgents());
        }

        if (newOptions.getScaleUpThreshold() >= 0) {
            options.setScaleUpThreshold(newOptions.getScaleUpThreshold());
        }

        if (newOptions.getScaleDownThreshold() >= 0) {
            options.setScaleDownThreshold(newOptions.getScaleDownThreshold());
        }

        if (newOptions.getScaleUpStep() > 0) {
            options.setScaleUpStep(newOptions.getScaleUpStep());
        }

        if (newOptions.getScaleDownStep() > 0) {
            options.setScaleDownStep(newOptions.getScaleDownStep());
        }

        if (newOptions.getCheckInterval() > 0) {
            options.setCheckInterval(newOptions.getCheckInterval());
        }

        // Etkinlik durumunu en son güncelle
        if (statusChanged) {
            setEnabled(newOptions.isEnabled());
        } else {
            // Seçenek değişikliği olayını yayınla
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("options", options);
            eventData.put("timestamp", LocalDateTime.now());

            AutoScalerEvent event = new AutoScalerEvent(
                    this,
                    "autoScalerOptionsChanged",
                    eventData
            );

            eventPublisher.publishEvent(event);
            webSocketService.sendAutoScalerStatus(options);
        }

        logger.info("Otomatik ölçeklendirme seçenekleri güncellendi: {}", options);
    }

    /**
     * Kuyruk durumunu kontrol et ve ölçeklendir
     * Bu metot belirli aralıklarla çalışır
     */
    @Scheduled(fixedDelayString = "${app.autoscaler.check-interval:10000}")
    public void checkAndScale() {
        if (!options.isEnabled()) {
            return;
        }

        try {
            // Kuyruk ve havuz durumunu al
            TestQueueService.QueueStatus queueStatus = testQueueService.getQueueStatus();
            AgentPoolService.PoolStatus poolStatus = agentPoolService.getPoolStatus();

            // Mevcut agent sayısı
            int currentAgents = poolStatus.getTotalAgents();

            // Kuyruk uzunluğu
            int queueLength = queueStatus.getLength();

            // Boşta agent sayısı
            int idleAgents = poolStatus.getIdleAgents();

            // Ölçeklendirme kararı
            int targetAgents = currentAgents;

            // Yukarı ölçeklendirme: Kuyrukta bekleyen test sayısı eşik değerinden fazlaysa ve boşta agent yoksa
            if (queueLength >= options.getScaleUpThreshold() && idleAgents == 0) {
                // Yukarı ölçeklendirme adımı kadar agent ekle, maksimum sınırı aşma
                targetAgents = Math.min(currentAgents + options.getScaleUpStep(), options.getMaxAgents());

                if (targetAgents > currentAgents) {
                    logger.info("Yukarı ölçeklendirme: {} -> {} (Kuyruk: {}, Boşta: {})",
                            currentAgents, targetAgents, queueLength, idleAgents);

                    // Agent havuzunu güncelle
                    for (int i = 0; i < targetAgents - currentAgents; i++) {
                        agentPoolService.createAgent();
                    }

                    // Ölçeklendirme olayını yayınla
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("previousAgents", currentAgents);
                    eventData.put("newAgents", targetAgents);
                    eventData.put("queueLength", queueLength);
                    eventData.put("idleAgents", idleAgents);
                    eventData.put("timestamp", LocalDateTime.now());

                    AutoScalerEvent event = new AutoScalerEvent(
                            this,
                            "autoScalerScaledUp",
                            eventData
                    );

                    eventPublisher.publishEvent(event);
                    webSocketService.sendAutoScalerEvent("scaled_up", eventData);
                }
            }
            // Aşağı ölçeklendirme: Kuyrukta bekleyen test sayısı eşik değerinden azsa ve boşta agent varsa
            else if (queueLength <= options.getScaleDownThreshold() && idleAgents > 1) {
                // Aşağı ölçeklendirme adımı kadar agent kaldır, minimum sınırın altına düşme
                targetAgents = Math.max(currentAgents - options.getScaleDownStep(), options.getMinAgents());

                if (targetAgents < currentAgents) {
                    logger.info("Aşağı ölçeklendirme: {} -> {} (Kuyruk: {}, Boşta: {})",
                            currentAgents, targetAgents, queueLength, idleAgents);

                    // Agent havuzunu küçült
                    agentPoolService.scaleDown();

                    // Ölçeklendirme olayını yayınla
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("previousAgents", currentAgents);
                    eventData.put("newAgents", targetAgents);
                    eventData.put("queueLength", queueLength);
                    eventData.put("idleAgents", idleAgents);
                    eventData.put("timestamp", LocalDateTime.now());

                    AutoScalerEvent event = new AutoScalerEvent(
                            this,
                            "autoScalerScaledDown",
                            eventData
                    );

                    eventPublisher.publishEvent(event);
                    webSocketService.sendAutoScalerEvent("scaled_down", eventData);
                }
            }
        } catch (Exception e) {
            logger.error("Otomatik ölçeklendirme kontrolü sırasında hata oluştu", e);
        }
    }

    /**
     * AutoScalerOptions sınıfı
     */
    @Getter
    @Setter
    public static class AutoScalerOptions {
        private boolean enabled = false;
        private long checkInterval = 10000; // 10 saniye
        private int minAgents = 3;
        private int maxAgents = 10;
        private int scaleUpThreshold = 2; // Kuyrukta 2 veya daha fazla test varsa ölçeklendir
        private int scaleDownThreshold = 0; // Kuyrukta test yoksa ölçeklendir
        private int scaleUpStep = 1; // Her seferde 1 agent ekle
        private int scaleDownStep = 1; // Her seferde 1 agent kaldır

        @Override
        public String toString() {
            return "AutoScalerOptions{" +
                    "enabled=" + enabled +
                    ", checkInterval=" + checkInterval +
                    ", minAgents=" + minAgents +
                    ", maxAgents=" + maxAgents +
                    ", scaleUpThreshold=" + scaleUpThreshold +
                    ", scaleDownThreshold=" + scaleDownThreshold +
                    ", scaleUpStep=" + scaleUpStep +
                    ", scaleDownStep=" + scaleDownStep +
                    '}';
        }
    }
}
