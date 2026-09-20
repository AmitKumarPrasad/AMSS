package com.edusphere.documents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId);
    List<Document> findBySchoolIdAndStatusOrderByCreatedAtDesc(UUID schoolId, String status);
    boolean existsBySchoolIdAndStorageKey(UUID schoolId, String storageKey);
    Page<Document> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId, Pageable pageable);
    Page<Document> findBySchoolIdAndStatusOrderByCreatedAtDesc(UUID schoolId, String status, Pageable pageable);
}
