package com.edusphere.student;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, UUID> {
    List<StudentGuardian> findByStudentIdAndStatusOrderByPrimaryContactDescFullNameAsc(UUID studentId, String status);
}
