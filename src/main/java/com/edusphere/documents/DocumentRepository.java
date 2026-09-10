package com.edusphere.documents;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId);
    List<Document> findBySchoolIdAndStatusOrderByCreatedAtDesc(UUID schoolId, String status);
    boolean existsBySchoolIdAndStorageKey(UUID schoolId, String storageKey);
}
