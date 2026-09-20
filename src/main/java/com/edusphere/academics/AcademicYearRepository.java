package com.edusphere.academics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
    @Query("""
            select y from AcademicYear y
            where y.schoolId = :schoolId
              and (:status is null or y.status = :status)
            order by y.startsOn desc, y.id desc
            """)
    Page<AcademicYear> search(@Param("schoolId") UUID schoolId, @Param("status") String status, Pageable pageable);
    boolean existsBySchoolIdAndStatus(UUID schoolId, String status);
    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
    boolean existsBySchoolIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(
            UUID schoolId, java.time.LocalDate endsOn, java.time.LocalDate startsOn);
}
