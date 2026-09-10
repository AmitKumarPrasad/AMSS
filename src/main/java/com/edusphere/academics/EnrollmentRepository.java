package com.edusphere.academics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByStudentIdAndSectionId(UUID studentId, UUID sectionId);
    List<Enrollment> findBySectionIdAndStatusOrderByEnrolledOnDesc(UUID sectionId, String status);
    List<Enrollment> findByStudentIdAndStatusOrderByEnrolledOnDesc(UUID studentId, String status);
}
