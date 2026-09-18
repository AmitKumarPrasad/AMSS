package com.edusphere.academics;

import com.edusphere.student.Student;
import com.edusphere.student.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final AcademicYearRepository academicYearRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
                             SectionRepository sectionRepository, SchoolClassRepository classRepository,
                             AcademicYearRepository academicYearRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.classRepository = classRepository;
        this.academicYearRepository = academicYearRepository;
    }

    @Transactional
    public EnrollmentView enroll(UUID schoolId, UUID sectionId, EnrollStudentRequest request) {
        if (request == null || request.studentId() == null || request.enrolledOn() == null) {
            bad("studentId and enrolledOn are required");
        }
        Section section = requireSection(schoolId, sectionId);
        SchoolClass schoolClass = classRepository.findById(section.getClassId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        AcademicYear academicYear = academicYearRepository.findById(schoolClass.getAcademicYearId())
                .filter(y -> schoolId.equals(y.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic year not found"));
        if (!"ACTIVE".equals(academicYear.getStatus())) bad("Enrollment can only be created for an active academic year");
        if (!"ACTIVE".equals(schoolClass.getStatus())) bad("Enrollment can only be created for an active class");
        if (!"ACTIVE".equals(section.getStatus())) bad("Enrollment can only be created for an active section");
        Student student = requireStudent(schoolId, request.studentId());
        if (!"ACTIVE".equals(student.getStatus())) bad("Enrollment can only be created for an active student");
        if (request.enrolledOn().isAfter(LocalDate.now())) bad("enrolledOn cannot be in the future");
        if (enrollmentRepository.findByStudentIdAndSectionId(student.getId(), sectionId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Enrollment already exists");
        }
        return toView(enrollmentRepository.save(new Enrollment(student.getId(), sectionId, request.enrolledOn())));
    }

    @Transactional
    public EnrollmentView changeStatus(UUID schoolId, UUID enrollmentId, boolean active) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        Student student = requireStudent(schoolId, enrollment.getStudentId());
        Section section = requireSection(schoolId, enrollment.getSectionId());
        SchoolClass schoolClass = classRepository.findById(section.getClassId())
                .filter(c -> schoolId.equals(c.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        AcademicYear academicYear = academicYearRepository.findById(schoolClass.getAcademicYearId())
                .filter(y -> schoolId.equals(y.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic year not found"));
        if (active && !"ACTIVE".equals(academicYear.getStatus())) bad("Enrollment can only be activated for an active academic year");
        if (active && !"ACTIVE".equals(schoolClass.getStatus())) bad("Enrollment can only be activated for an active class");
        if (active && !"ACTIVE".equals(section.getStatus())) bad("Enrollment can only be activated for an active section");
        if (active && !"ACTIVE".equals(student.getStatus())) bad("Enrollment can only be activated for an active student");
        if (active) enrollment.activate(); else enrollment.deactivate();
        return toView(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> listBySection(UUID schoolId, UUID sectionId) {
        requireSection(schoolId, sectionId);
        return enrollmentRepository.findBySectionIdAndStatusOrderByEnrolledOnDesc(sectionId, "ACTIVE").stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> listByStudent(UUID schoolId, UUID studentId) {
        requireStudent(schoolId, studentId);
        return enrollmentRepository.findByStudentIdAndStatusOrderByEnrolledOnDesc(studentId, "ACTIVE").stream().map(this::toView).toList();
    }

    private Section requireSection(UUID schoolId, UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        SchoolClass schoolClass = classRepository.findById(section.getClassId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        if (!schoolId.equals(schoolClass.getSchoolId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found");
        return section;
    }

    private Student requireStudent(UUID schoolId, UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!schoolId.equals(student.getSchoolId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        return student;
    }

    private EnrollmentView toView(Enrollment enrollment) {
        return new EnrollmentView(enrollment.getId(), enrollment.getStudentId(), enrollment.getSectionId(), enrollment.getEnrolledOn(), enrollment.getStatus());
    }

    private void bad(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }

    public record EnrollStudentRequest(UUID studentId, LocalDate enrolledOn) {}
    public record EnrollmentView(UUID id, UUID studentId, UUID sectionId, LocalDate enrolledOn, String status) {}
}