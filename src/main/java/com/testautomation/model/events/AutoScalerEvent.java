package com.testautomation.model.events;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * AutoScalerEvent
 * Otomatik ölçeklendirme olayları için event sınıfı
 */
public class AutoScalerEvent extends ApplicationEvent {
    private final String eventType;
    private final Map<String, Object> data;

    /**
     * AutoScalerEvent yapıcı metodu
     * @param source Olayı tetikleyen nesne
     * @param eventType Olay tipi
     * @param data Olay verileri
     */
    public AutoScalerEvent(Object source, String eventType, Map<String, Object> data) {
        super(source);
        this.eventType = eventType;
        this.data = data;
    }

    /**
     * Olay tipini al
     * @return Olay tipi
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Olay verilerini al
     * @return Olay verileri
     */
    public Map<String, Object> getData() {
        return data;
    }
}
