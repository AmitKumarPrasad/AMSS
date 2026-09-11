package com.edusphere.notifications;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeliveryScheduler {
    private final NotificationService service;

    public NotificationDeliveryScheduler(NotificationService service) { this.service = service; }

    @Scheduled(fixedDelayString = "${notifications.delivery.fixed-delay-ms:30000}")
    public void deliverPending() { service.deliverableBatch(); }
}
