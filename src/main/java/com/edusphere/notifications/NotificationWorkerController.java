package com.edusphere.notifications;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/worker")
public class NotificationWorkerController {
    private final NotificationWorkerService worker;

    public NotificationWorkerController(NotificationWorkerService worker) {
        this.worker = worker;
    }

    @PostMapping("/claim")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public List<Notification> claim(@RequestParam String workerId) {
        return worker.claimBatch(workerId);
    }

    @PostMapping("/release-expired")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public int releaseExpired() {
        return worker.releaseExpiredClaims();
    }

    @PostMapping("/{notificationId}/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public boolean process(@RequestParam String workerId, @PathVariable UUID notificationId) {
        return worker.process(workerId, notificationId);
    }
}
