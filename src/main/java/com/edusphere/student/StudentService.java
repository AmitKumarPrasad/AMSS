package com.edusphere.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StudentService {
    private static final int MAX_PAGE_SIZE = 100;

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public java.util.List<StudentView> activeStudents(UUID schoolId) {
        return list(schoolId, "ACTIVE", null, 0, MAX_PAGE_SIZE).getContent();
    }

    public Page<StudentView> list(UUID schoolId, String status, String query, int page, int size) {
        validatePage(page, size);
        String normalizedStatus = normalizeStatus(status);
        String normalizedQuery = normalizeQuery(query);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.asc("firstName"),
                Sort.Order.asc("lastName"),
                Sort.Order.asc("id")));
        Page<Student> students = normalizedQuery == null
                ? repository.findBySchoolIdAndStatus(schoolId, normalizedStatus, pageable)
                : repository.search(schoolId, normalizedStatus, normalizedQuery, pageable);
        return students.map(StudentView::from);
    }

    @Transactional
    public StudentView create(UUID schoolId, CreateStudentRequest request) {
        String admissionNumber = normalizeRequired(request.admissionNumber(), "admissionNumber");
        String firstName = normalizeRequired(request.firstName(), "firstName");
        String lastName = normalizeOptional(request.lastName());
        if (request.dateOfBirth() != null && request.dateOfBirth().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateOfBirth cannot be in the future");
        }
        if (repository.existsBySchoolIdAndAdmissionNumber(schoolId, admissionNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Admission number already exists for this school");
        }
        Student student = new Student(schoolId, admissionNumber, firstName, lastName, request.dateOfBirth());
        return StudentView.from(repository.save(student));
    }

    @Transactional
    public StudentView changeStatus(UUID schoolId, UUID studentId, boolean active) {
        Student student = repository.findById(studentId)
                .filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (active) student.activate(); else student.deactivate();
        return StudentView.from(student);
    }

    private String normalizeRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeStatus(String status) {
        String normalized = status == null || status.isBlank() ? "ACTIVE" : status.trim().toUpperCase();
        if (!normalized.equals("ACTIVE") && !normalized.equals("INACTIVE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be ACTIVE or INACTIVE");
        }
        return normalized;
    }

    private String normalizeQuery(String query) {
        return query == null || query.isBlank() ? null : query.trim();
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    public record CreateStudentRequest(String admissionNumber, String firstName, String lastName,
                                       LocalDate dateOfBirth) {}

    public record StudentView(UUID id, String admissionNumber, String firstName, String lastName, String status) {
        static StudentView from(Student s) {
            return new StudentView(s.getId(), s.getAdmissionNumber(), s.getFirstName(), s.getLastName(), s.getStatus());
        }
    }
}
