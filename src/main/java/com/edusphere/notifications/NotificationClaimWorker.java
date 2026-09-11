package com.edusphere.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class NotificationClaimWorker {
    private final NotificationClaimRepository claims;
    private final NotificationService service;
    private final String workerId;

    public NotificationClaimWorker(NotificationClaimRepository claims, NotificationService service,
                                   @Value("${notifications.worker-id:${HOSTNAME:amss-worker}}") String workerId) {
        this.claims = claims;
        this.service = service;
        this.workerId = workerId;
    }

    @Transactional
    public int processBatch() {
        Instant now = Instant.now();
        var ids = claims.claimPending(now, now.minusSeconds(300), workerId);
        int processed = 0;
        for (UUID id : ids) {
            if (service.deliverClaimed(id, workerId)) processed++;
        }
        return processed;
    }
}
