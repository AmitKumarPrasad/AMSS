package com.edusphere.academics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface SchoolClassRepository extends JpaRepository<SchoolClass,UUID>{
    @Query("""
            select c from SchoolClass c
            where c.schoolId = :schoolId
              and c.academicYearId = :academicYearId
            order by c.gradeLevel asc, c.name asc, c.id asc
            """)
    Page<SchoolClass> search(@Param("schoolId") UUID schoolId,@Param("academicYearId") UUID academicYearId,Pageable pageable);
    boolean existsBySchoolIdAndAcademicYearIdAndNameIgnoreCase(UUID schoolId, UUID academicYearId, String name);
}
