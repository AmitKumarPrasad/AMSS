package com.edusphere.assessments;

import com.edusphere.security.TenantAccess;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/report-cards")
public class ReportCardController {
    private final ReportCardService service;
    private final TenantAccess tenantAccess;

    public ReportCardController(ReportCardService service, TenantAccess tenantAccess) {
        this.service = service;
        this.tenantAccess = tenantAccess;
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ReportCardService.ReportCardView get(@PathVariable UUID schoolId,
                                                @PathVariable UUID studentId,
                                                @RequestParam UUID academicYearId,
                                                Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.get(schoolId, academicYearId, studentId);
    }
}
