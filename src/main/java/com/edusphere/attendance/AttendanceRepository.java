package com.edusphere.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {
    Optional<AttendanceRecord> findBySchoolIdAndStudentIdAndAttendanceDate(
            UUID schoolId, UUID studentId, LocalDate attendanceDate);

    List<AttendanceRecord> findBySchoolIdAndStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            UUID schoolId, UUID studentId, LocalDate from, LocalDate to);
}
