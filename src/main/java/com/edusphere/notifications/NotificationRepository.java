package com.edusphere.notifications;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findTop100ByStatusAndAvailableAtLessThanEqualAndClaimedByIsNullOrderByAvailableAtAsc(String status, Instant now);
    List<Notification> findBySchoolIdAndRecipientUserIdOrderByCreatedAtDesc(UUID schoolId, UUID recipientUserId);
    long countBySchoolIdAndRecipientUserIdAndReadAtIsNull(UUID schoolId, UUID recipientUserId);

    @Modifying
    @Query("update Notification n set n.claimedBy = :workerId, n.claimedAt = :claimedAt where n.id = :id and n.status = :status and n.availableAt <= :now and n.claimedBy is null")
    int claim(UUID id, String workerId, Instant claimedAt, Instant now, String status);

    @Modifying
    @Query("update Notification n set n.claimedBy = null, n.claimedAt = null where n.status = 'PENDING' and n.claimedBy is not null and n.claimedAt < :cutoff")
    int releaseExpiredClaims(Instant cutoff);
}
