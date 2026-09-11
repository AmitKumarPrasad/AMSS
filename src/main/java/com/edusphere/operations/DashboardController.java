package com.edusphere.operations;

import com.edusphere.security.TenantAccess;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/operations/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    private final TenantAccess tenantAccess;

    public DashboardController(DashboardService dashboardService, TenantAccess tenantAccess) {
        this.dashboardService = dashboardService;
        this.tenantAccess = tenantAccess;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public DashboardService.DashboardView summary(@PathVariable UUID schoolId, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return dashboardService.summary(schoolId);
    }
}
