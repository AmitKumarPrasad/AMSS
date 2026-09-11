package com.edusphere.academics;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/academics")
public class SubjectController {
    private final SubjectService service;
    private final TenantAccess tenantAccess;

    public SubjectController(SubjectService service, TenantAccess tenantAccess) {
        this.service = service;
        this.tenantAccess = tenantAccess;
    }

    @GetMapping("/subjects")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<SubjectService.SubjectView> list(@PathVariable UUID schoolId, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.list(schoolId);
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public SubjectService.SubjectView create(@PathVariable UUID schoolId,
                                             @Valid @RequestBody CreateSubjectRequest request,
                                             Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.create(schoolId, new SubjectService.CreateSubjectRequest(request.code(), request.name()));
    }

    @PostMapping("/classes/{classId}/subjects/{subjectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public SubjectService.ClassSubjectView assign(@PathVariable UUID schoolId, @PathVariable UUID classId,
                                                   @PathVariable UUID subjectId, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.assign(schoolId, classId, subjectId);
    }

    @GetMapping("/classes/{classId}/subjects")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<SubjectService.ClassSubjectView> listForClass(@PathVariable UUID schoolId,
                                                               @PathVariable UUID classId,
                                                               Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.listForClass(schoolId, classId);
    }

    public record CreateSubjectRequest(@NotBlank String code, @NotBlank String name) {}
}
