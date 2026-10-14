package com.edusphere.documents;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    boolean existsBySchoolIdAndStorageKey(UUID schoolId, String storageKey);
    Page<Document> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId, Pageable pageable);
    Page<Document> findBySchoolIdAndStatusOrderByCreatedAtDesc(UUID schoolId, String status, Pageable pageable);

    @Query("""
            select d from Document d where d.schoolId = :schoolId
            and (d.uploadedBy = :userId or d.audienceRole is null or d.audienceRole in :roles)
            order by d.createdAt desc
            """)
    Page<Document> findPublisherVisible(UUID schoolId, UUID userId, java.util.Collection<String> roles, Pageable pageable);

    @Query("""
            select d from Document d
            where d.schoolId = :schoolId
              and d.status = :status
              and (d.audienceRole is null or d.audienceRole in :roles)
            order by d.createdAt desc
            """)
    Page<Document> findPublishedVisible(UUID schoolId, String status, java.util.Collection<String> roles, Pageable pageable);
}