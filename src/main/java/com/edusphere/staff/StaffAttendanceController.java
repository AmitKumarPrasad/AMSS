package com.edusphere.staff;

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
@RequestMapping("/api/v1/schools/{schoolId}/staff-attendance")
public class StaffAttendanceController {
    private final StaffAttendanceService service;
    private final TenantAccess tenantAccess;

    public StaffAttendanceController(StaffAttendanceService service, TenantAccess tenantAccess) {
        this.service = service; this.tenantAccess = tenantAccess;
    }

    @PostMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public StaffAttendanceService.AttendanceView record(
            @PathVariable UUID schoolId, @PathVariable UUID staffId,
            @Valid @RequestBody RecordAttendance request, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.record(schoolId, staffId,
                new StaffAttendanceService.RecordAttendanceRequest(request.attendanceDate(), request.status()),
                tenantAccess.currentUserId(authentication));
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public List<StaffAttendanceService.AttendanceView> history(
            @PathVariable UUID schoolId, @PathVariable UUID staffId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.history(schoolId, staffId, from, to);
    }

    public record RecordAttendance(@NotNull LocalDate attendanceDate, @NotBlank String status) {}
}
