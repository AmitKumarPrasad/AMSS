package com.edusphere.assessments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, UUID> {
    Optional<AssessmentResult> findByAssessmentIdAndStudentId(UUID assessmentId, UUID studentId);
    List<AssessmentResult> findByAssessmentIdOrderByStudentIdAsc(UUID assessmentId);
}
