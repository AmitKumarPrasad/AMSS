package com.edusphere.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationWorkerServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Notification notification;

    @InjectMocks
    private NotificationWorkerService worker;

    @Test
    void claimBatchClaimsOnlyRowsWonByAtomicUpdate() {
        UUID notificationId = UUID.randomUUID();
        String workerId = "worker-1";

        when(notification.getId()).thenReturn(notificationId);
        when(repository.findTop100ByStatusAndAvailableAtLessThanEqualAndClaimedByIsNullOrderByAvailableAtAsc(
                eq("PENDING"), any(Instant.class))).thenReturn(List.of(notification));
        when(repository.claim(eq(notificationId), eq(workerId), any(Instant.class), any(Instant.class), eq("PENDING")))
                .thenReturn(1);

        List<Notification> claimed = worker.claimBatch(workerId);

        assertEquals(List.of(notification), claimed);
        verify(repository).claim(eq(notificationId), eq(workerId), any(Instant.class), any(Instant.class), eq("PENDING"));
    }

    @Test
    void claimBatchRejectsMissingWorkerId() {
        assertThrows(IllegalArgumentException.class, () -> worker.claimBatch(" "));
        verifyNoInteractions(repository);
    }

    @Test
    void releaseExpiredClaimsUsesRepositoryBulkUpdate() {
        when(repository.releaseExpiredClaims(any(Instant.class))).thenReturn(3);

        assertEquals(3, worker.releaseExpiredClaims());

        verify(repository).releaseExpiredClaims(any(Instant.class));
        verify(repository, never()).findAll();
    }

    @Test
    void processDelegatesToNotificationService() {
        UUID notificationId = UUID.randomUUID();
        when(notificationService.deliverClaimed(notificationId, "worker-1")).thenReturn(true);

        assertTrue(worker.process("worker-1", notificationId));

        verify(notificationService).deliverClaimed(notificationId, "worker-1");
    }
}
