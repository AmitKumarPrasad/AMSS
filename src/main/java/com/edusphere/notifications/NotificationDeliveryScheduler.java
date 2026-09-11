package com.edusphere.notifications;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeliveryScheduler {
    private final NotificationClaimWorker worker;

    public NotificationDeliveryScheduler(NotificationClaimWorker worker) { this.worker = worker; }

    @Scheduled(fixedDelayString = "${notifications.delivery.fixed-delay-ms:30000}")
    public void deliver() { worker.processBatch(); }
}
