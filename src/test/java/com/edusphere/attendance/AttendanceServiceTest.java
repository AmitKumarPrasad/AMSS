package com.edusphere.attendance;

import com.edusphere.student.Student;
import com.edusphere.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {
    @Mock AttendanceRepository attendanceRepository;
    @Mock StudentRepository studentRepository;
    AttendanceService service;

    @BeforeEach void setUp() { service = new AttendanceService(attendanceRepository, studentRepository); }

    @Test void rejectsAttendanceForInactiveStudent() {
        UUID school = UUID.randomUUID(), studentId = UUID.randomUUID();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student(school, "A1", "Amit", null, null)));
        Student student = studentRepository.findById(studentId).orElseThrow();
        student.deactivate();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.mark(school, studentId, LocalDate.now(), "PRESENT", UUID.randomUUID()));
        assertEquals(400, ex.getStatusCode().value());
        verify(attendanceRepository, never()).save(any());
    }

    @Test void normalizesStatusAndUpdatesExistingRecord() {
        UUID school = UUID.randomUUID(), studentId = UUID.randomUUID(), user = UUID.randomUUID();
        Student student = new Student(school, "A1", "Amit", null, null);
        AttendanceRecord existing = new AttendanceRecord(school, studentId, LocalDate.of(2026, 9, 18), "ABSENT", "MANUAL", user);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(attendanceRepository.findBySchoolIdAndStudentIdAndAttendanceDate(school, studentId, existing.getAttendanceDate()))
            .thenReturn(Optional.of(existing));
        when(attendanceRepository.save(existing)).thenReturn(existing);

        AttendanceService.AttendanceView view = service.mark(school, studentId, existing.getAttendanceDate(), " late ", user);

        assertEquals("LATE", view.status());
        verify(attendanceRepository).save(existing);
    }

    @Test void rejectsInvalidStatus() {
        UUID school = UUID.randomUUID(), studentId = UUID.randomUUID();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student(school, "A1", "Amit", null, null)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.mark(school, studentId, LocalDate.now(), "UNKNOWN", UUID.randomUUID()));
        assertEquals(400, ex.getStatusCode().value());
    }
}
