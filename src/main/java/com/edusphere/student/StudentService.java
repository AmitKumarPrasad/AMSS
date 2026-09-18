package com.edusphere.student;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository repository;
    public StudentService(StudentRepository repository) { this.repository = repository; }

    public List<StudentView> activeStudents(UUID schoolId) {
        return repository.findBySchoolIdAndStatusOrderByFirstNameAsc(schoolId, "ACTIVE")
                .stream().map(StudentView::from).toList();
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

    public record CreateStudentRequest(String admissionNumber, String firstName, String lastName,
                                       LocalDate dateOfBirth) {}

    public record StudentView(UUID id, String admissionNumber, String firstName, String lastName, String status) {
        static StudentView from(Student s) {
            return new StudentView(s.getId(), s.getAdmissionNumber(), s.getFirstName(), s.getLastName(), s.getStatus());
        }
    }
}
