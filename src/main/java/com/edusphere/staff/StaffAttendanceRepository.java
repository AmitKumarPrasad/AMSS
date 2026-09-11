package com.edusphere.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffAttendanceRepository extends JpaRepository<StaffAttendanceRecord, UUID> {
    Optional<StaffAttendanceRecord> findByStaffIdAndAttendanceDate(UUID staffId, LocalDate attendanceDate);
    List<StaffAttendanceRecord> findBySchoolIdAndStaffIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            UUID schoolId, UUID staffId, LocalDate from, LocalDate to);
}
