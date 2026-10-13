package com.edusphere.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Page<Student> findBySchoolIdAndStatus(UUID schoolId, String status, Pageable pageable);

    @Query("""
            select s from Student s
            where s.schoolId = :schoolId
              and s.status = :status
              and (
                    lower(s.firstName) like lower(concat('%', :query, '%'))
                    or lower(coalesce(s.lastName, '')) like lower(concat('%', :query, '%'))
                    or lower(s.admissionNumber) like lower(concat('%', :query, '%'))
              )
            """)
    Page<Student> search(@Param("schoolId") UUID schoolId,
                         @Param("status") String status,
                         @Param("query") String query,
                         Pageable pageable);

    boolean existsBySchoolIdAndAdmissionNumber(UUID schoolId, String admissionNumber);
}
