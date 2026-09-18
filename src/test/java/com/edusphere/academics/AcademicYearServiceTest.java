package com.edusphere.academics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicYearServiceTest {
    @Mock
    private AcademicYearRepository repository;

    private AcademicYearService service;

    @BeforeEach
    void setUp() {
        service = new AcademicYearService(repository);
    }

    @Test
    void rejectsReversedDates() {
        UUID schoolId = UUID.randomUUID();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.create(schoolId, new AcademicYearService.CreateAcademicYearRequest(
                        "2026-27", "2026-27", LocalDate.of(2027, 4, 1), LocalDate.of(2026, 4, 1))));
        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsDuplicateCode() {
        UUID schoolId = UUID.randomUUID();
        when(repository.existsBySchoolIdAndCode(schoolId, "2026-27")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.create(schoolId, new AcademicYearService.CreateAcademicYearRequest(
                        " 2026-27 ", "2026-27", LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31))));
        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsOverlappingDates() {
        UUID schoolId = UUID.randomUUID();
        when(repository.existsBySchoolIdAndCode(schoolId, "2027-28")).thenReturn(false);
        when(repository.existsBySchoolIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(
                schoolId, LocalDate.of(2028, 3, 31), LocalDate.of(2027, 4, 1))).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.create(schoolId, new AcademicYearService.CreateAcademicYearRequest(
                        "2027-28", "2027-28", LocalDate.of(2027, 4, 1), LocalDate.of(2028, 3, 31))));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void createsTrimmedAcademicYear() {
        UUID schoolId = UUID.randomUUID();
        when(repository.existsBySchoolIdAndCode(schoolId, "2028-29")).thenReturn(false);
        when(repository.existsBySchoolIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(
                schoolId, LocalDate.of(2029, 3, 31), LocalDate.of(2028, 4, 1))).thenReturn(false);
        when(repository.save(any(AcademicYear.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AcademicYearService.AcademicYearView view = service.create(schoolId,
                new AcademicYearService.CreateAcademicYearRequest(
                        " 2028-29 ", " 2028-29 Session ", LocalDate.of(2028, 4, 1), LocalDate.of(2029, 3, 31)));

        assertEquals("2028-29", view.code());
        assertEquals("2028-29 Session", view.name());
    }
}
