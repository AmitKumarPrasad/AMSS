package com.edusphere.academics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SchoolClassRepository extends JpaRepository<SchoolClass,UUID>{
    List<SchoolClass> findBySchoolIdAndAcademicYearIdOrderByGradeLevelAscNameAsc(UUID schoolId,UUID academicYearId);
}
