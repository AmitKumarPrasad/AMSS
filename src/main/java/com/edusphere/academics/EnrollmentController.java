package com.edusphere.academics;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    private final TenantAccess tenantAccess;

    public EnrollmentController(EnrollmentService enrollmentService, TenantAccess tenantAccess) {
        this.enrollmentService = enrollmentService; this.tenantAccess = tenantAccess;
    }

    @PostMapping("/sections/{sectionId}/students")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public EnrollmentService.EnrollmentView enroll(@PathVariable UUID schoolId, @PathVariable UUID sectionId,
                                                    @Valid @RequestBody EnrollStudentRequest request, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return enrollmentService.enroll(schoolId, sectionId,
                new EnrollmentService.EnrollStudentRequest(request.studentId(), request.enrolledOn()));
    }

    @PatchMapping("/{enrollmentId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public EnrollmentService.EnrollmentView changeStatus(@PathVariable UUID schoolId, @PathVariable UUID enrollmentId,
                                                          @RequestParam boolean active, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return enrollmentService.changeStatus(schoolId, enrollmentId, active);
    }

    @GetMapping("/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public Page<EnrollmentService.EnrollmentView> listBySection(@PathVariable UUID schoolId, @PathVariable UUID sectionId,
                                                                  @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                  @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
                                                                  Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return enrollmentService.listBySection(schoolId, sectionId, page, size);
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public Page<EnrollmentService.EnrollmentView> listByStudent(@PathVariable UUID schoolId, @PathVariable UUID studentId,
                                                                  @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                  @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
                                                                  Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return enrollmentService.listByStudent(schoolId, studentId, page, size);
    }

    public record EnrollStudentRequest(@NotNull UUID studentId, @NotNull LocalDate enrolledOn) {}
}