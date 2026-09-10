package com.edusphere.academics;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/academic-years")
public class AcademicYearController {
    private final AcademicYearService service; private final TenantAccess tenantAccess;
    public AcademicYearController(AcademicYearService service,TenantAccess tenantAccess){this.service=service;this.tenantAccess=tenantAccess;}

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<AcademicYearService.AcademicYearView> list(@PathVariable UUID schoolId,Authentication authentication){
        tenantAccess.requireSchool(authentication,schoolId); return service.list(schoolId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public AcademicYearService.AcademicYearView create(@PathVariable UUID schoolId,@Valid @RequestBody CreateRequest request,Authentication authentication){
        tenantAccess.requireSchool(authentication,schoolId);
        return service.create(schoolId,new AcademicYearService.CreateAcademicYearRequest(request.code(),request.name(),request.startsOn(),request.endsOn()));
    }

    public record CreateRequest(@NotBlank String code,@NotBlank String name,@NotNull LocalDate startsOn,@NotNull LocalDate endsOn){}
}
