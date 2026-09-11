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
@RequestMapping("/api/v1/schools/{schoolId}/staff")
public class StaffController {
    private final StaffService service; private final TenantAccess tenantAccess;
    public StaffController(StaffService service,TenantAccess tenantAccess){this.service=service;this.tenantAccess=tenantAccess;}
    @PostMapping @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public StaffService.StaffView create(@PathVariable UUID schoolId,@Valid @RequestBody CreateStaff r,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.create(schoolId,new StaffService.CreateStaffRequest(r.employeeCode(),r.fullName(),r.email(),r.phone(),r.designation(),r.employmentType(),r.joinedOn()));}
    @GetMapping @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<StaffService.StaffView> list(@PathVariable UUID schoolId,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.list(schoolId);}
    @PostMapping("/{staffId}/leave") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public StaffService.LeaveView leave(@PathVariable UUID schoolId,@PathVariable UUID staffId,@Valid @RequestBody CreateLeave r,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.requestLeave(schoolId,staffId,new StaffService.CreateLeaveRequest(r.leaveType(),r.startsOn(),r.endsOn(),r.reason()));}
    @GetMapping("/leave") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<StaffService.LeaveView> leaves(@PathVariable UUID schoolId,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.leaves(schoolId);}
    @PostMapping("/leave/{leaveId}/review") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public StaffService.LeaveView review(@PathVariable UUID schoolId,@PathVariable UUID leaveId,@RequestParam String status,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.review(schoolId,leaveId,status,tenantAccess.currentUserId(a));}
    public record CreateStaff(@NotBlank String employeeCode,@NotBlank String fullName,String email,String phone,@NotBlank String designation,String employmentType,LocalDate joinedOn){}
    public record CreateLeave(@NotBlank String leaveType,@NotNull LocalDate startsOn,@NotNull LocalDate endsOn,String reason){}
}
