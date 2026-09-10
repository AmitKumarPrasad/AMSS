package com.edusphere.attendance;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final TenantAccess tenantAccess;

    public AttendanceController(AttendanceService attendanceService, TenantAccess tenantAccess) {
        this.attendanceService = attendanceService;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public AttendanceService.AttendanceView mark(@PathVariable UUID schoolId,
                                                  @PathVariable UUID studentId,
                                                  @Valid @RequestBody MarkAttendanceRequest request,
                                                  Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return attendanceService.mark(schoolId, studentId, request.attendanceDate(),
                request.status(), tenantAccess.currentUserId(authentication));
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<AttendanceService.AttendanceView> history(@PathVariable UUID schoolId,
                                                            @PathVariable UUID studentId,
                                                            @RequestParam LocalDate from,
                                                            @RequestParam LocalDate to,
                                                            Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return attendanceService.history(schoolId, studentId, from, to);
    }

    public record MarkAttendanceRequest(@NotNull LocalDate attendanceDate, @NotBlank String status) {}
}
