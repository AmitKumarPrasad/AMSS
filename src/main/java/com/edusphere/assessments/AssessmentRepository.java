package com.edusphere.assessments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    Optional<Assessment> findByIdAndSchoolId(UUID id, UUID schoolId);
    Page<Assessment> findBySchoolIdAndAcademicYearIdOrderByAssessmentDateDesc(UUID schoolId, UUID academicYearId, Pageable pageable);
    List<Assessment> findBySchoolIdAndAcademicYearIdOrderByAssessmentDateDesc(UUID schoolId, UUID academicYearId);
    boolean existsByClassIdAndNameAndAssessmentDate(UUID classId, String name, java.time.LocalDate assessmentDate);
}
