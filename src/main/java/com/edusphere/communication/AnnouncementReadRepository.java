package com.edusphere.communication;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, AnnouncementReadId> {
    boolean existsByAnnouncementIdAndUserId(UUID announcementId, UUID userId);
}
