package com.edusphere.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, UUID> {
    Page<StudentGuardian> findByStudentIdAndStatus(UUID studentId, String status, Pageable pageable);
    List<StudentGuardian> findByStudentIdAndStatusOrderByPrimaryContactDescFullNameAsc(UUID studentId, String status);
}
