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

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public AttendanceView mark(UUID schoolId, UUID studentId, LocalDate date, String status, UUID recordedBy) {
        Student student = studentRepository.findById(studentId)
                .filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        if (date == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attendanceDate is required");
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "status must be one of PRESENT, ABSENT, LATE, EXCUSED");
        }

        AttendanceRecord record = attendanceRepository
                .findBySchoolIdAndStudentIdAndAttendanceDate(schoolId, student.getId(), date)
                .orElseGet(() -> new AttendanceRecord(schoolId, student.getId(), date,
                        normalizedStatus, "MANUAL", recordedBy));
        if (record.getId() != null) record.update(normalizedStatus, "MANUAL", recordedBy);
        return toView(attendanceRepository.save(record));
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
        return attendanceRepository
                .findBySchoolIdAndStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(schoolId, studentId, from, to)
                .stream().map(this::toView).toList();
    }

    private AttendanceView toView(AttendanceRecord record) {
        return new AttendanceView(record.getId(), record.getSchoolId(), record.getStudentId(),
                record.getAttendanceDate(), record.getStatus(), record.getSource(), record.getRecordedBy());
    }

    public record AttendanceView(UUID id, UUID schoolId, UUID studentId, LocalDate attendanceDate,
                                 String status, String source, UUID recordedBy) {}
}
