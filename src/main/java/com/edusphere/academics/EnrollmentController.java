package com.edusphere.academics;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    private final TenantAccess tenantAccess;

    public EnrollmentController(EnrollmentService enrollmentService, TenantAccess tenantAccess) {
        this.enrollmentService = enrollmentService;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping("/sections/{sectionId}/students")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public EnrollmentService.EnrollmentView enroll(@PathVariable UUID schoolId, @PathVariable UUID sectionId,
                                                    @Valid @RequestBody EnrollStudentRequest request,
                                                    Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return enrollmentService.enroll(schoolId, sectionId,
                new EnrollmentService.EnrollStudentRequest(request.studentId(), request.enrolledOn()));
    }

    @GetMapping("/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<EnrollmentService.EnrollmentView> listBySection(@PathVariable UUID schoolId,
                                                                 @PathVariable UUID sectionId,
                                                                 Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return enrollmentService.listBySection(schoolId, sectionId);
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<EnrollmentService.EnrollmentView> listByStudent(@PathVariable UUID schoolId,
                                                                 @PathVariable UUID studentId,
                                                                 Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return enrollmentService.listByStudent(schoolId, studentId);
    }

    public record EnrollStudentRequest(@NotNull UUID studentId, @NotNull LocalDate enrolledOn) {}
}
