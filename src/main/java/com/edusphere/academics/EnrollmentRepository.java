package com.edusphere.academics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByStudentIdAndSectionId(UUID studentId, UUID sectionId);
    List<Enrollment> findByStudentIdAndStatusOrderByEnrolledOnDesc(UUID studentId, String status);
    Page<Enrollment> findBySectionIdAndStatusOrderByEnrolledOnDesc(UUID sectionId, String status, Pageable pageable);
    Page<Enrollment> findByStudentIdAndStatusOrderByEnrolledOnDesc(UUID studentId, String status, Pageable pageable);
}
