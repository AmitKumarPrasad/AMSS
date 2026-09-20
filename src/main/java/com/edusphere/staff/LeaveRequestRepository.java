package com.edusphere.staff;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    Page<LeaveRequest> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId, Pageable pageable);
    List<LeaveRequest> findByStaffIdOrderByStartsOnDesc(UUID staffId);
    boolean existsByStaffIdAndStatusAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(UUID staffId, String status, LocalDate endsOn, LocalDate startsOn);
}
