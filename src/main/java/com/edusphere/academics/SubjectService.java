package com.edusphere.academics;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final SchoolClassRepository classRepository;

    public SubjectService(SubjectRepository subjectRepository, ClassSubjectRepository classSubjectRepository,
                          SchoolClassRepository classRepository) {
        this.subjectRepository = subjectRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.classRepository = classRepository;
    }

    @Transactional
    public SubjectView create(UUID schoolId, CreateSubjectRequest request) {
        if (schoolId == null || request == null) bad("schoolId and subject request are required");
        if (request.code() == null || request.code().isBlank()) bad("code is required");
        if (request.name() == null || request.name().isBlank()) bad("name is required");
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        String name = request.name().trim();
        if (subjectRepository.existsBySchoolIdAndCode(schoolId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subject code already exists");
        }
        Subject subject = subjectRepository.save(new Subject(schoolId, code, name));
        return toView(subject);
    }

    @Transactional(readOnly = true)
    public List<SubjectView> list(UUID schoolId) {
        if (schoolId == null) bad("schoolId is required");
        return subjectRepository.findBySchoolIdAndStatusOrderByNameAsc(schoolId, "ACTIVE")
                .stream().map(this::toView).toList();
    }

    @Transactional
    public ClassSubjectView assign(UUID schoolId, UUID classId, UUID subjectId) {
        if (schoolId == null || classId == null || subjectId == null) bad("schoolId, classId and subjectId are required");
        SchoolClass schoolClass = classRepository.findById(classId)
                .filter(value -> schoolId.equals(value.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .filter(value -> schoolId.equals(value.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        if (!"ACTIVE".equals(subject.getStatus())) bad("Only active subjects can be assigned");
        if (classSubjectRepository.findByClassIdAndSubjectId(schoolClass.getId(), subject.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subject is already assigned to class");
        }
        return toClassSubjectView(classSubjectRepository.save(new ClassSubject(classId, subjectId)), subject);
    }

    @Transactional(readOnly = true)
    public List<ClassSubjectView> listForClass(UUID schoolId, UUID classId) {
        if (schoolId == null || classId == null) bad("schoolId and classId are required");
        classRepository.findById(classId).filter(value -> schoolId.equals(value.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        return classSubjectRepository.findByClassIdAndStatusOrderBySubjectIdAsc(classId, "ACTIVE").stream()
                .map(link -> subjectRepository.findById(link.getSubjectId())
                        .map(subject -> toClassSubjectView(link, subject))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Assigned subject not found")))
                .toList();
    }

    private SubjectView toView(Subject value) {
        return new SubjectView(value.getId(), value.getSchoolId(), value.getCode(), value.getName(), value.getStatus());
    }

    private ClassSubjectView toClassSubjectView(ClassSubject link, Subject subject) {
        return new ClassSubjectView(link.getId(), link.getClassId(), subject.getId(), subject.getCode(), subject.getName(), link.getStatus());
    }

    private void bad(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }

    public record CreateSubjectRequest(String code, String name) {}
    public record SubjectView(UUID id, UUID schoolId, String code, String name, String status) {}
    public record ClassSubjectView(UUID id, UUID classId, UUID subjectId, String subjectCode, String subjectName, String status) {}
}
