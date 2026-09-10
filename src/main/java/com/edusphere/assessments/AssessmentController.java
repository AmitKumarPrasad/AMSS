package com.edusphere.assessments;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/assessments")
public class AssessmentController {
    private final AssessmentService assessmentService;
    private final TenantAccess tenantAccess;

    public AssessmentController(AssessmentService assessmentService, TenantAccess tenantAccess) {
        this.assessmentService = assessmentService;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public AssessmentService.AssessmentView create(@PathVariable UUID schoolId,
                                                     @Valid @RequestBody CreateAssessmentRequest request,
                                                     Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return assessmentService.create(schoolId, new AssessmentService.CreateAssessmentRequest(
                request.academicYearId(), request.classId(), request.name(), request.assessmentDate(), request.maxMarks()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<AssessmentService.AssessmentView> list(@PathVariable UUID schoolId,
                                                        @RequestParam UUID academicYearId,
                                                        Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return assessmentService.list(schoolId, academicYearId);
    }

    @PostMapping("/{assessmentId}/results/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public AssessmentService.ResultView recordResult(@PathVariable UUID schoolId,
                                                      @PathVariable UUID assessmentId,
                                                      @PathVariable UUID studentId,
                                                      @Valid @RequestBody RecordResultRequest request,
                                                      Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return assessmentService.recordResult(schoolId, assessmentId, studentId, request.marks(), request.grade(),
                tenantAccess.currentUserId(authentication));
    }

    @GetMapping("/{assessmentId}/results")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<AssessmentService.ResultView> results(@PathVariable UUID schoolId,
                                                       @PathVariable UUID assessmentId,
                                                       Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return assessmentService.results(schoolId, assessmentId);
    }

    public record CreateAssessmentRequest(@NotNull UUID academicYearId, @NotNull UUID classId,
                                          @NotBlank String name, @NotNull LocalDate assessmentDate,
                                          @NotNull @DecimalMin("0.01") BigDecimal maxMarks) {}

    public record RecordResultRequest(@NotNull @DecimalMin("0.00") BigDecimal marks, String grade) {}
}
