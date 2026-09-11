package com.edusphere.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StaffRepository extends JpaRepository<StaffMember, UUID> {
    boolean existsBySchoolIdAndEmployeeCode(UUID schoolId, String employeeCode);
    List<StaffMember> findBySchoolIdAndStatusOrderByFullNameAsc(UUID schoolId, String status);
}
