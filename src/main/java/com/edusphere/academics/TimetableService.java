package com.edusphere.academics;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TimetableService {
    private final TimetableEntryRepository repository;
    private final SectionRepository sectionRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectRepository classSubjectRepository;

    public TimetableService(TimetableEntryRepository repository, SectionRepository sectionRepository,
                            SchoolClassRepository classRepository, SubjectRepository subjectRepository,
                            ClassSubjectRepository classSubjectRepository) {
        this.repository = repository; this.sectionRepository = sectionRepository; this.classRepository = classRepository;
        this.subjectRepository = subjectRepository; this.classSubjectRepository = classSubjectRepository;
    }

    @Transactional
    public TimetableView create(UUID schoolId, UUID sectionId, CreateTimetableRequest request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        SchoolClass schoolClass = requireClass(schoolId, section.getClassId());
        Subject subject = subjectRepository.findById(request.subjectId())
                .filter(s -> schoolId.equals(s.getSchoolId()) && "ACTIVE".equals(s.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        if (classSubjectRepository.findByClassIdAndSubjectId(schoolClass.getId(), subject.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is not assigned to the class");
        }
        String day = normalizeDay(request.dayOfWeek());
        if (request.startsAt() == null || request.endsAt() == null || !request.startsAt().isBefore(request.endsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startsAt must be before endsAt");
        }
        if (!repository.findBySectionIdAndDayOfWeekAndStartsAtLessThanAndEndsAtGreaterThan(sectionId, day, request.endsAt(), request.startsAt()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Timetable entry overlaps an existing entry");
        }
        TimetableEntry saved = repository.save(new TimetableEntry(schoolId, sectionId, subject.getId(), day,
                request.startsAt(), request.endsAt(), trimNullable(request.room())));
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<TimetableView> list(UUID schoolId, UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        requireClass(schoolId, section.getClassId());
        return repository.findBySectionIdAndStatusOrderByDayOfWeekAscStartsAtAsc(sectionId, "ACTIVE")
                .stream().map(this::toView).toList();
    }

    private SchoolClass requireClass(UUID schoolId, UUID classId) {
        return classRepository.findById(classId).filter(c -> schoolId.equals(c.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }

    private String normalizeDay(String value) {
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dayOfWeek is required");
        try { return DayOfWeek.valueOf(value.trim().toUpperCase(Locale.ROOT)).name(); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid dayOfWeek"); }
    }

    private String trimNullable(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private TimetableView toView(TimetableEntry e) { return new TimetableView(e.getId(), e.getSchoolId(), e.getSectionId(), e.getSubjectId(), e.getDayOfWeek(), e.getStartsAt(), e.getEndsAt(), e.getRoom(), e.getStatus(), e.getCreatedAt()); }

    public record CreateTimetableRequest(UUID subjectId, String dayOfWeek, LocalTime startsAt, LocalTime endsAt, String room) {}
    public record TimetableView(UUID id, UUID schoolId, UUID sectionId, UUID subjectId, String dayOfWeek, LocalTime startsAt, LocalTime endsAt, String room, String status, java.time.Instant createdAt) {}
}
