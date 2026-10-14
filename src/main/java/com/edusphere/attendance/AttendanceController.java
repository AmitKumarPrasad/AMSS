package com.edusphere.attendance;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.format.annotation.DateTimeFormat;
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

    @PostMapping("/students/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<AttendanceService.AttendanceView> markBulk(@PathVariable UUID schoolId,
                                                            @Valid @RequestBody BulkAttendanceRequest request,
                                                            Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return attendanceService.markBulk(schoolId, request.attendanceDate(), request.entries(),
                tenantAccess.currentUserId(authentication));
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<AttendanceService.AttendanceView> history(@PathVariable UUID schoolId,
                                                            @PathVariable UUID studentId,
                                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                            Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return attendanceService.history(schoolId, studentId, from, to);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public AttendanceService.DailySummary summary(@PathVariable UUID schoolId,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                   Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return attendanceService.dailySummary(schoolId, date);
    }

    public record MarkAttendanceRequest(
            @NotNull LocalDate attendanceDate,
            @NotBlank @Size(max = 30) String status) {}

    public record BulkAttendanceRequest(
            @NotNull LocalDate attendanceDate,
            @NotEmpty @Size(max = 500) List<@Valid BulkEntry> entries) {}

    public record BulkEntry(
            @NotNull UUID studentId,
            @NotBlank @Size(max = 30) String status) {}
}
