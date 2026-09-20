package com.edusphere.communication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Collection;
import java.util.UUID;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    Page<Announcement> findBySchoolIdAndStatusOrderByPublishedAtDesc(UUID schoolId, String status, Pageable pageable);
    Page<Announcement> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId, Pageable pageable);

    @Query("""
            select a from Announcement a
            where a.schoolId = :schoolId
              and a.status = :status
              and (a.audienceRole is null or a.audienceRole in :roles)
            order by a.publishedAt desc
            """)
    Page<Announcement> findPublishedVisible(UUID schoolId, String status, Collection<String> roles, Pageable pageable);
}