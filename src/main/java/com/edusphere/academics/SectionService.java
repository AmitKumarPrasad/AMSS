package com.edusphere.academics;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class SectionService {
    private final SectionRepository sectionRepository;
    private final SchoolClassRepository classRepository;

    public SectionService(SectionRepository sectionRepository, SchoolClassRepository classRepository) {
        this.sectionRepository = sectionRepository;
        this.classRepository = classRepository;
    }

    public SectionView create(UUID schoolId, UUID classId, CreateSectionRequest request) {
        requireClass(schoolId, classId);
        String name = request.name().trim();
        String room = request.room() == null ? null : request.room().trim();
        return toView(sectionRepository.save(new Section(classId, name, room)));
    }

    public List<SectionView> list(UUID schoolId, UUID classId) {
        requireClass(schoolId, classId);
        return sectionRepository.findByClassIdOrderByNameAsc(classId).stream().map(this::toView).toList();
    }

    private SchoolClass requireClass(UUID schoolId, UUID classId) {
        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        if (!schoolId.equals(schoolClass.getSchoolId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found");
        }
        return schoolClass;
    }

    private SectionView toView(Section section) {
        return new SectionView(section.getId(), section.getClassId(), section.getName(), section.getRoom());
    }

    public record CreateSectionRequest(String name, String room) {}
    public record SectionView(UUID id, UUID classId, String name, String room) {}
}
