package com.edusphere.student;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    List<Student> findBySchoolIdAndStatusOrderByFirstNameAsc(UUID schoolId, String status);
}
