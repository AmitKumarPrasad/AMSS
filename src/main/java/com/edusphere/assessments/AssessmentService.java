package com.edusphere.assessments;

import com.edusphere.academics.AcademicYearRepository;
import com.edusphere.academics.Enrollment;
import com.edusphere.academics.EnrollmentRepository;
import com.edusphere.academics.SchoolClass;
import com.edusphere.academics.SchoolClassRepository;
import com.edusphere.academics.SectionRepository;
import com.edusphere.student.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AssessmentService {
    private final AssessmentRepository assessmentRepository;
    private final AssessmentResultRepository resultRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             AssessmentResultRepository resultRepository,
                             AcademicYearRepository academicYearRepository,
                             SchoolClassRepository classRepository,
                             StudentRepository studentRepository,
                             EnrollmentRepository enrollmentRepository,
                             SectionRepository sectionRepository) {
        this.assessmentRepository = assessmentRepository;
        this.resultRepository = resultRepository;
        this.academicYearRepository = academicYearRepository;
        this.classRepository = classRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional
    public AssessmentView create(UUID schoolId, CreateAssessmentRequest request) {
        requireAcademicYear(schoolId, request.academicYearId());
        requireClass(schoolId, request.classId());
        if (!request.academicYearId().equals(requireClass(schoolId, request.classId()).getAcademicYearId())) {
            bad("class does not belong to the academic year");
        }
        if (request.maxMarks().signum() <= 0) bad("maxMarks must be greater than zero");
        String name = request.name().trim();
        if (assessmentRepository.existsByClassIdAndNameAndAssessmentDate(request.classId(), name, request.assessmentDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assessment already exists");
        }
        return toView(assessmentRepository.save(new Assessment(schoolId, request.academicYearId(),
                request.classId(), name, request.assessmentDate(), request.maxMarks())));
    }

    @Transactional(readOnly = true)
    public List<AssessmentView> list(UUID schoolId, UUID academicYearId) {
        requireAcademicYear(schoolId, academicYearId);
        return assessmentRepository.findBySchoolIdAndAcademicYearIdOrderByAssessmentDateDesc(schoolId, academicYearId)
                .stream().map(this::toView).toList();
    }

    @Transactional
    public ResultView recordResult(UUID schoolId, UUID assessmentId, UUID studentId,
                                   BigDecimal marks, String grade, UUID recordedBy) {
        Assessment assessment = assessmentRepository.findByIdAndSchoolId(assessmentId, schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        if (marks.signum() < 0 || marks.compareTo(assessment.getMaxMarks()) > 0) {
            bad("marks must be between zero and maxMarks");
        }
        requireEnrolledInClass(schoolId, assessment.getClassId(), studentId);
        String normalizedGrade = grade == null || grade.isBlank() ? null : grade.trim().toUpperCase();
        AssessmentResult result = resultRepository.findByAssessmentIdAndStudentId(assessmentId, studentId)
                .orElseGet(() -> new AssessmentResult(assessmentId, studentId, marks, normalizedGrade, recordedBy));
        if (result.getId() != null) result.update(marks, normalizedGrade, recordedBy);
        return toView(resultRepository.save(result));
    }

    @Transactional(readOnly = true)
    public List<ResultView> results(UUID schoolId, UUID assessmentId) {
        assessmentRepository.findByIdAndSchoolId(assessmentId, schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        return resultRepository.findByAssessmentIdOrderByStudentIdAsc(assessmentId).stream().map(this::toView).toList();
    }

    private AcademicYearMarker requireAcademicYear(UUID schoolId, UUID academicYearId) {
        return academicYearRepository.findById(academicYearId)
                .filter(y -> schoolId.equals(y.getSchoolId()))
                .map(y -> new AcademicYearMarker(y.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic year not found"));
    }

    private SchoolClass requireClass(UUID schoolId, UUID classId) {
        return classRepository.findById(classId)
                .filter(c -> schoolId.equals(c.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }

    private void requireEnrolledInClass(UUID schoolId, UUID classId, UUID studentId) {
        studentRepository.findById(studentId)
                .filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        boolean enrolled = enrollmentRepository.findByStudentIdAndStatusOrderByEnrolledOnDesc(studentId, "ACTIVE")
                .stream()
                .map(Enrollment::getSectionId)
                .map(sectionRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .anyMatch(section -> section.getClassId().equals(classId));
        if (!enrolled) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is not actively enrolled in the assessment class");
    }

    private AssessmentView toView(Assessment a) {
        return new AssessmentView(a.getId(), a.getSchoolId(), a.getAcademicYearId(), a.getClassId(), a.getName(),
                a.getAssessmentDate(), a.getMaxMarks(), a.getStatus());
    }

    private ResultView toView(AssessmentResult r) {
        return new ResultView(r.getId(), r.getAssessmentId(), r.getStudentId(), r.getMarks(), r.getGrade(),
                r.getStatus(), r.getRecordedBy(), r.getRecordedAt());
    }

    private void bad(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }

    private record AcademicYearMarker(UUID id) {}

    public record CreateAssessmentRequest(UUID academicYearId, UUID classId, String name,
                                          LocalDate assessmentDate, BigDecimal maxMarks) {}
    public record AssessmentView(UUID id, UUID schoolId, UUID academicYearId, UUID classId, String name,
                                 LocalDate assessmentDate, BigDecimal maxMarks, String status) {}
    public record ResultView(UUID id, UUID assessmentId, UUID studentId, BigDecimal marks, String grade,
                             String status, UUID recordedBy, Instant recordedAt) {}
}
