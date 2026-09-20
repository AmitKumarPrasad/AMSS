package com.edusphere.assessments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock AssessmentRepository assessments;
    @Mock AssessmentResultRepository results;
    @Mock com.edusphere.academics.AcademicYearRepository academicYears;
    @Mock com.edusphere.academics.SchoolClassRepository classes;
    @Mock com.edusphere.student.StudentRepository students;
    @Mock com.edusphere.academics.EnrollmentRepository enrollments;
    @Mock com.edusphere.academics.SectionRepository sections;

    private AssessmentService service;

    @BeforeEach
    void setUp() {
        service = new AssessmentService(assessments, results, academicYears, classes, students, enrollments, sections);
    }

    @Test
    void publishesDraftAssessment() {
        UUID schoolId = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        Assessment assessment = new Assessment(schoolId, UUID.randomUUID(), UUID.randomUUID(),
                "Math Unit Test", java.time.LocalDate.now(), java.math.BigDecimal.valueOf(100));

        when(assessments.findByIdAndSchoolId(assessmentId, schoolId)).thenReturn(Optional.of(assessment));
        when(assessments.save(any(Assessment.class))).thenAnswer(i -> i.getArgument(0));

        AssessmentService.AssessmentView view = service.changeStatus(schoolId, assessmentId, " published ");

        assertEquals("PUBLISHED", view.status());
        verify(assessments).save(assessment);
    }

    @Test
    void rejectsInvalidAssessmentStatus() {
        UUID schoolId = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        Assessment assessment = new Assessment(schoolId, UUID.randomUUID(), UUID.randomUUID(),
                "Math Unit Test", java.time.LocalDate.now(), java.math.BigDecimal.valueOf(100));

        when(assessments.findByIdAndSchoolId(assessmentId, schoolId)).thenReturn(Optional.of(assessment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(schoolId, assessmentId, "ARCHIVED"));

        assertEquals(400, ex.getStatusCode().value());
        verify(assessments, never()).save(any());
    }

    @Test
    void cancelledAssessmentCannotBePublished() {
        UUID schoolId = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        Assessment assessment = new Assessment(schoolId, UUID.randomUUID(), UUID.randomUUID(),
                "Math Unit Test", java.time.LocalDate.now(), java.math.BigDecimal.valueOf(100));
        assessment.changeStatus("CANCELLED");

        when(assessments.findByIdAndSchoolId(assessmentId, schoolId)).thenReturn(Optional.of(assessment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(schoolId, assessmentId, "PUBLISHED"));

        assertEquals(400, ex.getStatusCode().value());
        verify(assessments, never()).save(any());
    }

    @Test
    void assessmentStatusLookupIsTenantScoped() {
        UUID schoolId = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();

        when(assessments.findByIdAndSchoolId(assessmentId, schoolId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(schoolId, assessmentId, "PUBLISHED"));

        assertEquals(404, ex.getStatusCode().value());
        verify(assessments, never()).save(any());
    }
}
