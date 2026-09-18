package com.edusphere.notifications;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findTop100ByStatusAndAvailableAtLessThanEqualAndClaimedByIsNullOrderByAvailableAtAsc(String status, Instant now);
    List<Notification> findBySchoolIdAndRecipientUserIdOrderByCreatedAtDesc(UUID schoolId, UUID recipientUserId);
    long countBySchoolIdAndRecipientUserIdAndReadAtIsNull(UUID schoolId, UUID recipientUserId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update Notification n set n.claimedBy = :workerId, n.claimedAt = :claimedAt where n.id = :id and n.status = :status and n.availableAt <= :now and n.claimedBy is null")
    int claim(UUID id, String workerId, Instant claimedAt, Instant now, String status);
}
