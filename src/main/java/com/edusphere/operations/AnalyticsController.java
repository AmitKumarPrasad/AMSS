package com.edusphere.operations;

import com.edusphere.security.TenantAccess;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/operations/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final TenantAccess tenantAccess;

    public AnalyticsController(AnalyticsService analyticsService, TenantAccess tenantAccess) {
        this.analyticsService = analyticsService;
        this.tenantAccess = tenantAccess;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public AnalyticsService.AnalyticsView analytics(
            @PathVariable UUID schoolId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return analyticsService.analytics(schoolId, from, to);
    }
}
