package com.edusphere.staff;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class StaffAttendanceService {
    private static final List<String> STATUSES = List.of("PRESENT", "ABSENT", "LATE", "EXCUSED");
    private final StaffAttendanceRepository attendanceRepository;
    private final StaffRepository staffRepository;

    public StaffAttendanceService(StaffAttendanceRepository attendanceRepository, StaffRepository staffRepository) {
        this.attendanceRepository = attendanceRepository;
        this.staffRepository = staffRepository;
    }

    @Transactional
    public AttendanceView record(UUID schoolId, UUID staffId, RecordAttendanceRequest request, UUID recordedBy) {
        requireActiveStaff(schoolId, staffId);
        String status = normalizeStatus(request.status());
        LocalDate date = request.attendanceDate();
        if (date == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attendanceDate is required");

        StaffAttendanceRecord record = attendanceRepository.findByStaffIdAndAttendanceDate(staffId, date)
                .filter(existing -> schoolId.equals(existing.getSchoolId()))
                .map(existing -> { existing.update(status, recordedBy); return existing; })
                .orElseGet(() -> new StaffAttendanceRecord(schoolId, staffId, date, status, recordedBy));
        return view(attendanceRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<AttendanceView> history(UUID schoolId, UUID staffId, LocalDate from, LocalDate to) {
        requireActiveStaff(schoolId, staffId);
        LocalDate effectiveTo = to == null ? LocalDate.now() : to;
        LocalDate effectiveFrom = from == null ? effectiveTo.minusDays(30) : from;
        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
        }
        return attendanceRepository.findBySchoolIdAndStaffIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        schoolId, staffId, effectiveFrom, effectiveTo).stream().map(this::view).toList();
    }

    private StaffMember requireActiveStaff(UUID schoolId, UUID staffId) {
        return staffRepository.findById(staffId)
                .filter(s -> schoolId.equals(s.getSchoolId()) && "ACTIVE".equals(s.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        String status = value.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(status)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid attendance status");
        return status;
    }

    private AttendanceView view(StaffAttendanceRecord r) {
        return new AttendanceView(r.getId(), r.getSchoolId(), r.getStaffId(), r.getAttendanceDate(), r.getStatus(), r.getRecordedBy(), r.getRecordedAt());
    }

    public record RecordAttendanceRequest(LocalDate attendanceDate, String status) {}
    public record AttendanceView(UUID id, UUID schoolId, UUID staffId, LocalDate attendanceDate, String status, UUID recordedBy, java.time.Instant recordedAt) {}
}
