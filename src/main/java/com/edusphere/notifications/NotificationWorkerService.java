package com.edusphere.notifications;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationWorkerService {
    private final NotificationRepository repository;
    private final NotificationService notificationService;

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
        List<Notification> candidates =
                repository.findTop100ByStatusAndAvailableAtLessThanEqualAndClaimedByIsNullOrderByAvailableAtAsc("PENDING", now);
        return candidates.stream()
                .filter(n -> repository.claim(n.getId(), workerId, now, now, "PENDING") == 1)
                .map(n -> repository.findById(n.getId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public boolean process(String workerId, UUID notificationId) {
        return notificationService.deliverClaimed(notificationId, workerId);
    }
}
