package com.edusphere.assessments;

import com.edusphere.academics.AcademicYearRepository;
import com.edusphere.academics.Enrollment;
import com.edusphere.academics.EnrollmentRepository;
import com.edusphere.academics.SchoolClass;
import com.edusphere.academics.SchoolClassRepository;
import com.edusphere.academics.SectionRepository;
import com.edusphere.student.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        if (request == null || request.academicYearId() == null || request.classId() == null || request.assessmentDate() == null || request.maxMarks() == null) bad("academicYearId, classId, assessmentDate and maxMarks are required");
        if (request.name() == null || request.name().isBlank()) bad("name is required");
        requireActiveAcademicYear(schoolId, request.academicYearId());
        SchoolClass schoolClass = requireClass(schoolId, request.classId());
        if (!"ACTIVE".equals(schoolClass.getStatus())) bad("Assessment can only be created for an active class");
        if (!request.academicYearId().equals(schoolClass.getAcademicYearId())) bad("class does not belong to the academic year");
        if (request.assessmentDate().isAfter(LocalDate.now())) bad("assessmentDate cannot be in the future");
        if (request.maxMarks().signum() <= 0) bad("maxMarks must be greater than zero");
        String name = request.name().trim();
        if (assessmentRepository.existsByClassIdAndNameAndAssessmentDate(request.classId(), name, request.assessmentDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assessment already exists");
        }
        return toView(assessmentRepository.save(new Assessment(schoolId, request.academicYearId(),
                request.classId(), name, request.assessmentDate(), request.maxMarks())));
    }

    @Transactional(readOnly = true)
    public Page<AssessmentView> list(UUID schoolId, UUID academicYearId, int page, int size) {
        requireAcademicYear(schoolId, academicYearId);
        if (page < 0) bad("page must be >= 0");
        if (size < 1 || size > 100) bad("size must be between 1 and 100");
        return assessmentRepository.findBySchoolIdAndAcademicYearIdOrderByAssessmentDateDesc(
                schoolId, academicYearId,
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("assessmentDate"), Sort.Order.desc("id"))))
                .map(this::toView);
    }

    @Transactional
    public AssessmentView changeStatus(UUID schoolId, UUID assessmentId, String status) {
        if (assessmentId == null || status == null || status.isBlank()) bad("assessmentId and status are required");
        Assessment assessment = assessmentRepository.findByIdAndSchoolId(assessmentId, schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        String normalizedStatus = status.trim().toUpperCase();
        if (!List.of("DRAFT", "PUBLISHED", "CANCELLED").contains(normalizedStatus)) {
            bad("status must be one of DRAFT, PUBLISHED or CANCELLED");
        }
        if ("PUBLISHED".equals(normalizedStatus) && "CANCELLED".equals(assessment.getStatus())) {
            bad("Cancelled assessment cannot be published");
        }
        assessment.changeStatus(normalizedStatus);
        return toView(assessmentRepository.save(assessment));
    }

    @Transactional
    public ResultView recordResult(UUID schoolId, UUID assessmentId, UUID studentId,
                                   BigDecimal marks, String grade, UUID recordedBy) {
        if (assessmentId == null || studentId == null || marks == null) bad("assessmentId, studentId and marks are required");
        Assessment assessment = assessmentRepository.findByIdAndSchoolId(assessmentId, schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        if (!"DRAFT".equals(assessment.getStatus())) {
            bad("Assessment results can only be recorded while the assessment is in DRAFT status");
        }
        if (marks.signum() < 0 || marks.compareTo(assessment.getMaxMarks()) > 0) bad("marks must be between zero and maxMarks");
        requireEnrolledInClass(schoolId, assessment.getClassId(), studentId);
        String normalizedGrade = normalizeGrade(grade);
        AssessmentResult result = resultRepository.findByAssessmentIdAndStudentId(assessmentId, studentId)
                .orElseGet(() -> new AssessmentResult(assessmentId, studentId, marks, normalizedGrade, recordedBy));
        if (result.getId() != null) result.update(marks, normalizedGrade, recordedBy);
        return toView(resultRepository.save(result));
    }

    @Transactional
    public List<ResultView> recordResults(UUID schoolId, UUID assessmentId, List<ResultEntry> entries, UUID recordedBy) {
        if (entries == null || entries.isEmpty()) bad("entries are required");
        Set<UUID> seen = new HashSet<>();
        List<ResultView> results = new java.util.ArrayList<>();
        for (ResultEntry entry : entries) {
            if (entry == null || entry.studentId() == null || entry.marks() == null) bad("Each result requires studentId and marks");
            if (!seen.add(entry.studentId())) bad("Duplicate studentId in result batch");
            results.add(recordResult(schoolId, assessmentId, entry.studentId(), entry.marks(), entry.grade(), recordedBy));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<ResultView> results(UUID schoolId, UUID assessmentId) {
        assessmentRepository.findByIdAndSchoolId(assessmentId, schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        return resultRepository.findByAssessmentIdOrderByStudentIdAsc(assessmentId).stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public ReportCardView reportCard(UUID schoolId, UUID studentId, UUID academicYearId) {
        var student = studentRepository.findById(studentId)
                .filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        requireAcademicYear(schoolId, academicYearId);
        var assessments = assessmentRepository.findBySchoolIdAndAcademicYearIdOrderByAssessmentDateDesc(schoolId, academicYearId);
        var assessmentIds = assessments.stream().map(Assessment::getId).toList();
        if (assessmentIds.isEmpty()) return new ReportCardView(studentId, academicYearId, 0, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
        var results = resultRepository.findByAssessmentIdInAndStudentId(assessmentIds, studentId);
        BigDecimal obtained = results.stream().map(AssessmentResult::getMarks).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maximum = assessments.stream().map(Assessment::getMaxMarks).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal percentage = maximum.signum() == 0 ? BigDecimal.ZERO : obtained.multiply(BigDecimal.valueOf(100)).divide(maximum, 2, RoundingMode.HALF_UP);
        List<ResultView> entries = results.stream().map(this::toView).toList();
        return new ReportCardView(student.getId(), academicYearId, results.size(), obtained, percentage, entries);
    }

    private String normalizeGrade(String grade) {
        if (grade == null || grade.isBlank()) return null;
        String normalized = grade.trim().toUpperCase();
        if (!List.of("A+","A","B+","B","C+","C","D","E","F").contains(normalized)) bad("grade must be one of A+, A, B+, B, C+, C, D, E or F");
        return normalized;
    }

    private void requireActiveAcademicYear(UUID schoolId, UUID academicYearId) {
        var year = academicYearRepository.findById(academicYearId).filter(y -> schoolId.equals(y.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic year not found"));
        if (!"ACTIVE".equals(year.getStatus())) bad("Assessment can only be created for an active academic year");
    }

    private AcademicYearMarker requireAcademicYear(UUID schoolId, UUID academicYearId) {
        return academicYearRepository.findById(academicYearId).filter(y -> schoolId.equals(y.getSchoolId()))
                .map(y -> new AcademicYearMarker(y.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic year not found"));
    }

    private SchoolClass requireClass(UUID schoolId, UUID classId) {
        return classRepository.findById(classId).filter(c -> schoolId.equals(c.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }

    private void requireEnrolledInClass(UUID schoolId, UUID classId, UUID studentId) {
        var student = studentRepository.findById(studentId).filter(s -> schoolId.equals(s.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!"ACTIVE".equals(student.getStatus())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assessment results can only be recorded for an active student");
        boolean enrolled = enrollmentRepository.findByStudentIdAndStatusOrderByEnrolledOnDesc(studentId, "ACTIVE").stream()
                .map(Enrollment::getSectionId).map(sectionRepository::findById).filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get).anyMatch(section -> section.getClassId().equals(classId));
        if (!enrolled) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is not actively enrolled in the assessment class");
    }

    private AssessmentView toView(Assessment a) {
        return new AssessmentView(a.getId(), a.getSchoolId(), a.getAcademicYearId(), a.getClassId(), a.getName(), a.getAssessmentDate(), a.getMaxMarks(), a.getStatus());
    }
    private ResultView toView(AssessmentResult r) {
        return new ResultView(r.getId(), r.getAssessmentId(), r.getStudentId(), r.getMarks(), r.getGrade(), r.getStatus(), r.getRecordedBy(), r.getRecordedAt());
    }
    private void bad(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private record AcademicYearMarker(UUID id) {}

    public record CreateAssessmentRequest(UUID academicYearId, UUID classId, String name, LocalDate assessmentDate, BigDecimal maxMarks) {}
    public record ResultEntry(UUID studentId, BigDecimal marks, String grade) {}
    public record AssessmentView(UUID id, UUID schoolId, UUID academicYearId, UUID classId, String name, LocalDate assessmentDate, BigDecimal maxMarks, String status) {}
    public record ResultView(UUID id, UUID assessmentId, UUID studentId, BigDecimal marks, String grade, String status, UUID recordedBy, Instant recordedAt) {}
    public record ReportCardView(UUID studentId, UUID academicYearId, int resultCount, BigDecimal totalObtained, BigDecimal percentage, List<ResultView> results) {}
}
