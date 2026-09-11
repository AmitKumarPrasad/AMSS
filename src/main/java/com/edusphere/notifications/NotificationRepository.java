package com.edusphere.notifications;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findTop100ByStatusAndAvailableAtLessThanEqualOrderByAvailableAtAsc(String status, Instant now);
    List<Notification> findBySchoolIdAndRecipientUserIdOrderByCreatedAtDesc(UUID schoolId, UUID recipientUserId);
    long countBySchoolIdAndRecipientUserIdAndReadAtIsNull(UUID schoolId, UUID recipientUserId);
}
