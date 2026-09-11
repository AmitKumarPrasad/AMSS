package com.edusphere.academics;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/timetable")
public class TimetableController {
    private final TimetableService service;
    private final TenantAccess tenantAccess;

    public TimetableController(TimetableService service, TenantAccess tenantAccess) {
        this.service = service; this.tenantAccess = tenantAccess;
    }

    @PostMapping("/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public TimetableService.TimetableView create(@PathVariable UUID schoolId, @PathVariable UUID sectionId,
                                                  @Valid @RequestBody CreateRequest request, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.create(schoolId, sectionId, new TimetableService.CreateTimetableRequest(request.subjectId(), request.dayOfWeek(), request.startsAt(), request.endsAt(), request.room()));
    }

    @GetMapping("/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<TimetableService.TimetableView> list(@PathVariable UUID schoolId, @PathVariable UUID sectionId, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.list(schoolId, sectionId);
    }

    public record CreateRequest(@NotNull UUID subjectId, @NotBlank String dayOfWeek,
                                @NotNull LocalTime startsAt, @NotNull LocalTime endsAt, String room) {}
}
