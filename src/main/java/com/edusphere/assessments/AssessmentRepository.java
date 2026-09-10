package com.edusphere.assessments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    Optional<Assessment> findByIdAndSchoolId(UUID id, UUID schoolId);
    List<Assessment> findBySchoolIdAndAcademicYearIdOrderByAssessmentDateDesc(UUID schoolId, UUID academicYearId);
    boolean existsByClassIdAndNameAndAssessmentDate(UUID classId, String name, java.time.LocalDate assessmentDate);
}
