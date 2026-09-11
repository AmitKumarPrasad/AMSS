package com.edusphere.assessments;

import com.edusphere.academics.AcademicYearRepository;
import com.edusphere.academics.Enrollment;
import com.edusphere.academics.EnrollmentRepository;
import com.edusphere.student.Student;
import com.edusphere.student.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReportCardService {
    private final AssessmentRepository assessmentRepository;
    private final AssessmentResultRepository resultRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final AcademicYearRepository academicYearRepository;

    public ReportCardService(AssessmentRepository assessmentRepository, AssessmentResultRepository resultRepository,
                             EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
                             AcademicYearRepository academicYearRepository) {
        this.assessmentRepository = assessmentRepository;
        this.resultRepository = resultRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.academicYearRepository = academicYearRepository;
    }

    @Transactional(readOnly = true)
    public ReportCardView get(UUID schoolId, UUID academicYearId, UUID studentId) {
        academicYearRepository.findById(academicYearId).filter(y -> schoolId.equals(y.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic year not found"));
        Student student = studentRepository.findById(studentId).filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        List<Assessment> assessments = assessmentRepository
                .findBySchoolIdAndAcademicYearIdOrderByAssessmentDateDesc(schoolId, academicYearId);
        List<SubjectResult> results = assessments.stream().map(a -> resultRepository
                .findByAssessmentIdAndStudentId(a.getId(), studentId)
                .map(r -> new SubjectResult(a.getId(), a.getName(), a.getAssessmentDate(),
                        a.getMaxMarks(), r.getMarks(), r.getGrade(), r.getStatus()))
                .orElse(null)).filter(java.util.Objects::nonNull).toList();

        BigDecimal max = results.stream().map(SubjectResult::maxMarks).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal obtained = results.stream().map(SubjectResult::marks).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal percentage = max.signum() == 0 ? BigDecimal.ZERO : obtained.multiply(BigDecimal.valueOf(100))
                .divide(max, 2, RoundingMode.HALF_UP);
        return new ReportCardView(student.getId(), schoolId, academicYearId, student.getAdmissionNumber(),
                student.getFirstName(), student.getLastName(), results, max, obtained, percentage, results.size());
    }

    public record SubjectResult(UUID assessmentId, String assessmentName, LocalDate assessmentDate,
                                BigDecimal maxMarks, BigDecimal marks, String grade, String status) {}

    public record ReportCardView(UUID studentId, UUID schoolId, UUID academicYearId, String admissionNumber,
                                 String firstName, String lastName, List<SubjectResult> results,
                                 BigDecimal maxMarks, BigDecimal obtainedMarks, BigDecimal percentage,
                                 int assessedCount) {}
}
