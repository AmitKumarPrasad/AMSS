package com.edusphere.notifications;

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
    private static final long CLAIM_LEASE_SECONDS = 300;

    public NotificationWorkerService(NotificationRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
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
        Instant cutoff = Instant.now().minusSeconds(CLAIM_LEASE_SECONDS);
        return repository.releaseExpiredClaims(cutoff);
    }

    @Transactional
    public boolean process(String workerId, UUID notificationId) {
        return notificationService.deliverClaimed(notificationId, workerId);
    }
}
