package com.edusphere.academics;

import com.edusphere.student.Student;
import com.edusphere.student.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final SchoolClassRepository classRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
                             SectionRepository sectionRepository, SchoolClassRepository classRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.classRepository = classRepository;
    }

    public EnrollmentView enroll(UUID schoolId, UUID sectionId, EnrollStudentRequest request) {
        requireSection(schoolId, sectionId);
        Student student = requireStudent(schoolId, request.studentId());
        if (enrollmentRepository.findByStudentIdAndSectionId(student.getId(), sectionId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Enrollment already exists");
        }
        return toView(enrollmentRepository.save(new Enrollment(student.getId(), sectionId, request.enrolledOn())));
    }

    public List<EnrollmentView> listBySection(UUID schoolId, UUID sectionId) {
        requireSection(schoolId, sectionId);
        return enrollmentRepository.findBySectionIdAndStatusOrderByEnrolledOnDesc(sectionId, "ACTIVE")
                .stream().map(this::toView).toList();
    }

    public List<EnrollmentView> listByStudent(UUID schoolId, UUID studentId) {
        requireStudent(schoolId, studentId);
        return enrollmentRepository.findByStudentIdAndStatusOrderByEnrolledOnDesc(studentId, "ACTIVE")
                .stream().map(this::toView).toList();
    }

    private Section requireSection(UUID schoolId, UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        SchoolClass schoolClass = classRepository.findById(section.getClassId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        if (!schoolId.equals(schoolClass.getSchoolId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found");
        }
        return section;
    }

    private Student requireStudent(UUID schoolId, UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!schoolId.equals(student.getSchoolId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        return student;
    }

    private EnrollmentView toView(Enrollment enrollment) {
        return new EnrollmentView(enrollment.getId(), enrollment.getStudentId(), enrollment.getSectionId(),
                enrollment.getEnrolledOn(), enrollment.getStatus());
    }

    public record EnrollStudentRequest(UUID studentId, LocalDate enrolledOn) {}
    public record EnrollmentView(UUID id, UUID studentId, UUID sectionId, LocalDate enrolledOn, String status) {}
}
