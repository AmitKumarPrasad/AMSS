package com.edusphere.student;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public record StudentView(UUID id, String admissionNumber, String firstName, String lastName, String status) {
        static StudentView from(Student s) {
            return new StudentView(s.getId(), s.getAdmissionNumber(), s.getFirstName(), s.getLastName(), s.getStatus());
        }
    }
}
