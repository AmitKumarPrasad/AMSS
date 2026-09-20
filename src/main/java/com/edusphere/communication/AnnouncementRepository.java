package com.edusphere.communication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    List<Announcement> findBySchoolIdAndStatusOrderByPublishedAtDesc(UUID schoolId, String status);
    List<Announcement> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId);
    Page<Announcement> findBySchoolIdAndStatusOrderByPublishedAtDesc(UUID schoolId, String status, Pageable pageable);
    Page<Announcement> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId, Pageable pageable);
}
