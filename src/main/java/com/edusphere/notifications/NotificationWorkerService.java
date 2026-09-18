package com.edusphere.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationWorkerService {
    private final NotificationRepository repository;
    private final NotificationService notificationService;
    private final long claimLeaseSeconds;

    public NotificationWorkerService(NotificationRepository repository,
                                     NotificationService notificationService,
                                     @Value("${notifications.worker.claim-lease-seconds:300}") long claimLeaseSeconds) {
        if (claimLeaseSeconds <= 0) {
            throw new IllegalArgumentException("Notification claim lease must be greater than zero");
        }
        this.repository = repository;
        this.notificationService = notificationService;
        this.claimLeaseSeconds = claimLeaseSeconds;
    }

    @Transactional
    public List<Notification> claimBatch(String workerId) {
        if (workerId == null || workerId.isBlank()) {
            throw new IllegalArgumentException("workerId is required");
        }

        Instant now = Instant.now();
        return repository.findTop100ByStatusAndAvailableAtLessThanEqualAndClaimedByIsNullOrderByAvailableAtAsc("PENDING", now)
                .stream()
                .filter(n -> repository.claim(n.getId(), workerId, now, now, "PENDING") == 1)
                .toList();
    }

    @Scheduled(fixedDelayString = "${notifications.worker.interval-ms:10000}")
    public void runWorker() {
        releaseExpiredClaims();
        String workerId = "amss-worker-" + UUID.randomUUID();
        claimBatch(workerId).forEach(n -> process(workerId, n.getId()));
    }

    @Transactional
    public int releaseExpiredClaims() {
        Instant cutoff = Instant.now().minusSeconds(claimLeaseSeconds);
        return repository.releaseExpiredClaims(cutoff);
    }

    @Transactional
    public boolean process(String workerId, UUID notificationId) {
        return notificationService.deliverClaimed(notificationId, workerId);
    }
}
