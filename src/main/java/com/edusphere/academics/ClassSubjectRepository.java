package com.edusphere.academics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassSubjectRepository extends JpaRepository<ClassSubject, UUID> {
    Optional<ClassSubject> findByClassIdAndSubjectId(UUID classId, UUID subjectId);
    List<ClassSubject> findByClassIdAndStatusOrderBySubjectIdAsc(UUID classId, String status);
}
