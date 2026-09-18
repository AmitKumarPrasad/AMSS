package com.edusphere.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    private StudentRepository repository;

    private StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService(repository);
    }

    @Test
    void createRejectsDuplicateAdmissionNumber() {
        UUID schoolId = UUID.randomUUID();
        when(repository.existsBySchoolIdAndAdmissionNumber(schoolId, "ADM-1")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(schoolId, new StudentService.CreateStudentRequest(" ADM-1 ", "Amit", null, null)));

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).save(any(Student.class));
    }

    @Test
    void createRejectsFutureDateOfBirth() {
        UUID schoolId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(schoolId, new StudentService.CreateStudentRequest(
                        "ADM-2", "Amit", null, LocalDate.now().plusDays(1))));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void createNormalizesInputBeforeSaving() {
        UUID schoolId = UUID.randomUUID();
        when(repository.existsBySchoolIdAndAdmissionNumber(schoolId, "ADM-3")).thenReturn(false);
        when(repository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentService.StudentView view = service.create(schoolId,
                new StudentService.CreateStudentRequest(" ADM-3 ", " Amit ", " Kumar ", null));

        assertEquals("ADM-3", view.admissionNumber());
        assertEquals("Amit", view.firstName());
        assertEquals("Kumar", view.lastName());
    }

    @Test
    void changeStatusEnforcesSchoolTenant() {
        UUID schoolId = UUID.randomUUID();
        UUID otherSchoolId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Student student = new Student(otherSchoolId, "ADM-4", "Amit", null, null);
        when(repository.findById(studentId)).thenReturn(Optional.of(student));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(schoolId, studentId, false));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void changeStatusDeactivatesStudent() {
        UUID schoolId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Student student = new Student(schoolId, "ADM-5", "Amit", null, null);
        when(repository.findById(studentId)).thenReturn(Optional.of(student));

        StudentService.StudentView view = service.changeStatus(schoolId, studentId, false);

        assertEquals("INACTIVE", view.status());
    }
}
