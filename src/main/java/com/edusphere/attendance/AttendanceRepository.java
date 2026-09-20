package com.edusphere.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {
    Optional<AttendanceRecord> findBySchoolIdAndStudentIdAndAttendanceDate(
            UUID schoolId, UUID studentId, LocalDate attendanceDate);

    List<AttendanceRecord> findBySchoolIdAndStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            UUID schoolId, UUID studentId, LocalDate from, LocalDate to);

    @Query("""
            select new com.edusphere.attendance.AttendanceRepository$SummaryCounts(
                count(a),
                sum(case when a.status = 'PRESENT' then 1 else 0 end),
                sum(case when a.status = 'ABSENT' then 1 else 0 end),
                sum(case when a.status = 'LATE' then 1 else 0 end),
                sum(case when a.status = 'EXCUSED' then 1 else 0 end)
            )
            from AttendanceRecord a
            where a.schoolId = :schoolId and a.attendanceDate = :date
            """)
    SummaryCounts summary(UUID schoolId, LocalDate date);

    record SummaryCounts(long recorded, long present, long absent, long late, long excused) {}
}
