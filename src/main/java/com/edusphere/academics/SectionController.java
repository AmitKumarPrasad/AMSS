package com.edusphere.academics;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/classes/{classId}/sections")
public class SectionController {
    private final SectionService sectionService;
    private final TenantAccess tenantAccess;

    public SectionController(SectionService sectionService, TenantAccess tenantAccess) {
        this.sectionService = sectionService;
        this.tenantAccess = tenantAccess;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<SectionService.SectionView> list(@PathVariable UUID schoolId, @PathVariable UUID classId,
                                                  Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return sectionService.list(schoolId, classId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public SectionService.SectionView create(@PathVariable UUID schoolId, @PathVariable UUID classId,
                                              @Valid @RequestBody CreateSectionRequest request,
                                              Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return sectionService.create(schoolId, classId,
                new SectionService.CreateSectionRequest(request.name(), request.room()));
    }

    public record CreateSectionRequest(@NotBlank String name, String room) {}
}
