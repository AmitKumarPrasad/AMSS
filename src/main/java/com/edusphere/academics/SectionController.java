package com.edusphere.academics;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/schools/{schoolId}/classes/{classId}/sections")
public class SectionController {
    private final SectionService sectionService; private final TenantAccess tenantAccess;
    public SectionController(SectionService sectionService,TenantAccess tenantAccess){this.sectionService=sectionService;this.tenantAccess=tenantAccess;}
    @GetMapping @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<SectionService.SectionView> list(@PathVariable UUID schoolId,@PathVariable UUID classId,Authentication a){tenantAccess.requireSchool(a,schoolId);return sectionService.list(schoolId,classId);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public SectionService.SectionView create(@PathVariable UUID schoolId,@PathVariable UUID classId,@Valid @RequestBody CreateSectionRequest request,Authentication a){tenantAccess.requireSchool(a,schoolId);return sectionService.create(schoolId,classId,new SectionService.CreateSectionRequest(request.name(),request.room()));}
    @PatchMapping("/{sectionId}/status") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public SectionService.SectionView changeStatus(@PathVariable UUID schoolId,@PathVariable UUID classId,@PathVariable UUID sectionId,@RequestParam boolean active,Authentication a){tenantAccess.requireSchool(a,schoolId);return sectionService.changeStatus(schoolId,sectionId,active);}
    public record CreateSectionRequest(@NotBlank String name,String room){}
}
