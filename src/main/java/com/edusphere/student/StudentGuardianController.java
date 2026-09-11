package com.edusphere.student;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/students/{studentId}/guardians")
public class StudentGuardianController {
    private final StudentGuardianService service;
    private final TenantAccess tenantAccess;

    public StudentGuardianController(StudentGuardianService service, TenantAccess tenantAccess) {
        this.service = service;
        this.tenantAccess = tenantAccess;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT')")
    public List<StudentGuardianService.GuardianView> list(@PathVariable UUID schoolId, @PathVariable UUID studentId, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.list(schoolId, studentId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public StudentGuardianService.GuardianView create(@PathVariable UUID schoolId, @PathVariable UUID studentId,
                                                       @Valid @RequestBody CreateGuardian request, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.create(schoolId, studentId, new StudentGuardianService.CreateGuardianRequest(
                request.fullName(), request.relationship(), request.email(), request.phone(), request.primaryContact()));
    }

    public record CreateGuardian(@NotBlank String fullName, @NotBlank String relationship, @Email String email,
                                 String phone, boolean primaryContact) {}
}
