package com.edusphere.attendance;

import com.edusphere.student.Student;
import com.edusphere.student.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AttendanceService {
    private static final Set<String> VALID_STATUSES = Set.of("PRESENT", "ABSENT", "LATE", "EXCUSED");
    private static final int MAX_HISTORY_DAYS = 366;

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public AttendanceView mark(UUID schoolId, UUID studentId, LocalDate date, String status, UUID recordedBy) {
        validateDate(date);
        Student student = requireActiveStudent(schoolId, studentId);
        return save(student, date, normalizeStatus(status), recordedBy);
    }

    @Transactional
    public List<AttendanceView> markBulk(UUID schoolId, LocalDate date,
                                         List<AttendanceController.BulkEntry> entries, UUID recordedBy) {
        validateDate(date);
        if (entries == null || entries.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "entries are required");
        }

        Set<UUID> uniqueStudentIds = entries.stream()
                .map(AttendanceController.BulkEntry::studentId)
                .collect(java.util.stream.Collectors.toSet());
        if (uniqueStudentIds.size() != entries.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "entries must not contain duplicate studentId values");
        }

        return entries.stream()
                .map(entry -> {
                    Student student = requireActiveStudent(schoolId, entry.studentId());
                    return save(student, date, normalizeStatus(entry.status()), recordedBy);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceView> history(UUID schoolId, UUID studentId, LocalDate from, LocalDate to) {
        studentRepository.findById(studentId)
                .filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to are required");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1 > MAX_HISTORY_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attendance history range must not exceed 366 days");
        }
        return attendanceRepository
                .findBySchoolIdAndStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        schoolId, studentId, from, to)
                .stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public DailySummary dailySummary(UUID schoolId, LocalDate date) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required");
        }
        AttendanceRepository.SummaryCounts counts = attendanceRepository.summary(schoolId, date);
        return new DailySummary(date, counts.recorded(), counts.present(), counts.absent(),
                counts.late(), counts.excused());
    }

    private Student requireActiveStudent(UUID schoolId, UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!"ACTIVE".equals(student.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attendance can only be recorded for an active student");
        }
        return student;
    }

    private AttendanceView save(Student student, LocalDate date, String status, UUID recordedBy) {
        AttendanceRecord record = attendanceRepository
                .findBySchoolIdAndStudentIdAndAttendanceDate(student.getSchoolId(), student.getId(), date)
                .orElseGet(() -> new AttendanceRecord(student.getSchoolId(), student.getId(), date,
                        status, "MANUAL", recordedBy));
        if (record.getId() != null) {
            record.update(status, "MANUAL", recordedBy);
        }
        return toView(attendanceRepository.save(record));
    }

    private static void validateDate(LocalDate date) {
        if (date == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attendanceDate is required");
        if (date.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attendanceDate cannot be in the future");
        }
    }

    private static String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!VALID_STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "status must be one of PRESENT, ABSENT, LATE, EXCUSED");
        }
        return normalized;
    }

    private AttendanceView toView(AttendanceRecord record) {
        return new AttendanceView(record.getId(), record.getSchoolId(), record.getStudentId(),
                record.getAttendanceDate(), record.getStatus(), record.getSource(), record.getRecordedBy());
    }

    public record AttendanceView(UUID id, UUID schoolId, UUID studentId, LocalDate attendanceDate,
                                 String status, String source, UUID recordedBy) {}

    public record DailySummary(LocalDate date, long recorded, long present, long absent,
                               long late, long excused) {}
}
