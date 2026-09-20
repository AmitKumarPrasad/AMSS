package com.edusphere.staff;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/schools/{schoolId}/staff")
public class StaffController {
    private final StaffService service; private final TenantAccess tenantAccess;
    public StaffController(StaffService service,TenantAccess tenantAccess){this.service=service;this.tenantAccess=tenantAccess;}
    @PostMapping @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public StaffService.StaffView create(@PathVariable UUID schoolId,@Valid @RequestBody CreateStaff r,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.create(schoolId,new StaffService.CreateStaffRequest(r.employeeCode(),r.fullName(),r.email(),r.phone(),r.designation(),r.employmentType(),r.joinedOn()));}
    @PatchMapping("/{staffId}/status") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public StaffService.StaffView changeStatus(@PathVariable UUID schoolId,@PathVariable UUID staffId,@RequestParam boolean active,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.changeStatus(schoolId,staffId,active);}
    @GetMapping @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public Page<StaffService.StaffView> list(@PathVariable UUID schoolId,@RequestParam(defaultValue="ACTIVE") String status,@RequestParam(required=false) String q,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.list(schoolId,status,q,page,size);}
    @PostMapping("/{staffId}/leave") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public StaffService.LeaveView leave(@PathVariable UUID schoolId,@PathVariable UUID staffId,@Valid @RequestBody CreateLeave r,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.requestLeave(schoolId,staffId,new StaffService.CreateLeaveRequest(r.leaveType(),r.startsOn(),r.endsOn(),r.reason()));}
    @GetMapping("/leave") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public Page<StaffService.LeaveView> leaves(@PathVariable UUID schoolId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size,@RequestParam(required=false) String status,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.leaves(schoolId,page,size,status);}
    @PostMapping("/leave/{leaveId}/review") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public StaffService.LeaveView review(@PathVariable UUID schoolId,@PathVariable UUID leaveId,@RequestParam String status,Authentication a){tenantAccess.requireSchool(a,schoolId);return service.review(schoolId,leaveId,status,tenantAccess.currentUserId(a));}
    public record CreateStaff(@NotBlank @Size(max = 50) String employeeCode,@NotBlank @Size(max = 150) String fullName,@Email @Size(max = 254) String email,@Size(max = 30) String phone,@NotBlank @Size(max = 100) String designation,@Size(max = 50) String employmentType,LocalDate joinedOn){}
    public record CreateLeave(@NotBlank @Size(max = 50) String leaveType,@NotNull LocalDate startsOn,@NotNull LocalDate endsOn,@Size(max = 1000) String reason){}
}
