package com.edusphere.academics;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/classes")
public class SchoolClassController {
    private final SchoolClassService service; private final TenantAccess tenantAccess;
    public SchoolClassController(SchoolClassService service,TenantAccess tenantAccess){this.service=service;this.tenantAccess=tenantAccess;}
    @GetMapping @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<SchoolClassService.ClassView> list(@PathVariable UUID schoolId,@RequestParam UUID academicYearId,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.list(schoolId,academicYearId);}
    @PostMapping @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public SchoolClassService.ClassView create(@PathVariable UUID schoolId,@Valid @RequestBody CreateRequest r,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.create(schoolId,r.academicYearId(),new SchoolClassService.CreateClassRequest(r.name(),r.gradeLevel()));}
    public record CreateRequest(@NotNull UUID academicYearId,@NotBlank String name,@NotNull Integer gradeLevel){}
}
