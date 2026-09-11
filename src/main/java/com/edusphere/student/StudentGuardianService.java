package com.edusphere.student;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class StudentGuardianService {
    private static final List<String> RELATIONSHIPS = List.of("FATHER", "MOTHER", "GUARDIAN", "GRANDFATHER", "GRANDMOTHER", "OTHER");
    private final StudentGuardianRepository repository;
    private final StudentRepository studentRepository;

    public StudentGuardianService(StudentGuardianRepository repository, StudentRepository studentRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public GuardianView create(UUID schoolId, UUID studentId, CreateGuardianRequest request) {
        Student student = studentRepository.findById(studentId)
                .filter(s -> schoolId.equals(s.getSchoolId()) && "ACTIVE".equals(s.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        String relationship = request.relationship().trim().toUpperCase(Locale.ROOT);
        if (!RELATIONSHIPS.contains(relationship)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid relationship");
        }
        if (request.primaryContact()) {
            repository.findByStudentIdAndStatusOrderByPrimaryContactDescFullNameAsc(student.getId(), "ACTIVE")
                    .forEach(this::unsetPrimary);
        }
        StudentGuardian guardian = new StudentGuardian(schoolId, studentId, request.fullName().trim(), relationship,
                trim(request.email()), trim(request.phone()), request.primaryContact());
        return view(repository.save(guardian));
    }

    @Transactional(readOnly = true)
    public List<GuardianView> list(UUID schoolId, UUID studentId) {
        requireStudent(schoolId, studentId);
        return repository.findByStudentIdAndStatusOrderByPrimaryContactDescFullNameAsc(studentId, "ACTIVE")
                .stream().map(this::view).toList();
    }

    private void unsetPrimary(StudentGuardian guardian) {
        try {
            var field = StudentGuardian.class.getDeclaredField("primaryContact");
            field.setAccessible(true);
            field.setBoolean(guardian, false);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to update primary guardian", e);
        }
        repository.save(guardian);
    }

    private void requireStudent(UUID schoolId, UUID studentId) {
        studentRepository.findById(studentId)
                .filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private GuardianView view(StudentGuardian g) {
        return new GuardianView(g.getId(), g.getSchoolId(), g.getStudentId(), g.getFullName(), g.getRelationship(),
                g.getEmail(), g.getPhone(), g.isPrimaryContact(), g.getStatus(), g.getCreatedAt());
    }

    public record CreateGuardianRequest(String fullName, String relationship, String email, String phone, boolean primaryContact) {}
    public record GuardianView(UUID id, UUID schoolId, UUID studentId, String fullName, String relationship,
                               String email, String phone, boolean primaryContact, String status, java.time.Instant createdAt) {}
}
