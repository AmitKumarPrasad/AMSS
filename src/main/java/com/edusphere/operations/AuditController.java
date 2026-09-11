package com.edusphere.operations;

import com.edusphere.security.TenantAccess;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/operations/audit")
public class AuditController {
    private final AuditService auditService;
    private final TenantAccess tenantAccess;

    public AuditController(AuditService auditService, TenantAccess tenantAccess) {
        this.auditService = auditService;
        this.tenantAccess = tenantAccess;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public List<AuditService.AuditView> recent(@PathVariable UUID schoolId,
                                                @RequestParam(defaultValue = "50") int limit,
                                                Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return auditService.recent(schoolId, limit);
    }
}
