package com.edusphere.academics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
    List<AcademicYear> findBySchoolIdOrderByStartsOnDesc(UUID schoolId);
    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
    boolean existsBySchoolIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(
            UUID schoolId, java.time.LocalDate endsOn, java.time.LocalDate startsOn);
}
