package com.edusphere.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId);
    List<LeaveRequest> findByStaffIdOrderByStartsOnDesc(UUID staffId);
    boolean existsByStaffIdAndStatusAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(UUID staffId, String status, LocalDate endsOn, LocalDate startsOn);
}
