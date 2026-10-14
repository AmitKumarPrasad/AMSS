package com.edusphere.operations;
import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/setup")
public class SchoolSetupController {
    private final SchoolSetupService service; private final TenantAccess access;
    public SchoolSetupController(SchoolSetupService service,TenantAccess access){this.service=service;this.access=access;}
    @GetMapping("/profile")
    public SchoolSetupService.SchoolProfile profile(@PathVariable UUID schoolId,Authentication auth){access.requireSchool(auth,schoolId);return service.profile(schoolId,access.currentUserId(auth));}
    @PostMapping("/production-school") @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public SchoolSetupService.SetupResult setup(@PathVariable UUID schoolId,@Valid @RequestBody SetupRequest r,Authentication auth){access.requireSchool(auth,schoolId);return service.setup(schoolId,access.currentUserId(auth),r.schoolName(),r.timezone(),r.username(),r.email(),r.displayName(),r.password());}
    @PatchMapping("/profile") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public SchoolSetupService.SchoolProfile update(@PathVariable UUID schoolId,@Valid @RequestBody ProfileRequest r,Authentication auth){access.requireSchool(auth,schoolId);return service.updateProfile(schoolId,access.currentUserId(auth),r.schoolName(),r.timezone());}
    @GetMapping("/users") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public List<SchoolSetupService.UserView> users(@PathVariable UUID schoolId,@RequestParam(defaultValue="0") @Min(0) int page,Authentication auth){access.requireSchool(auth,schoolId);return service.users(schoolId,page);}
    @PostMapping("/users") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public SchoolSetupService.UserView createUser(@PathVariable UUID schoolId,@Valid @RequestBody UserRequest r,Authentication auth){access.requireSchool(auth,schoolId);return service.createUser(schoolId,r.username(),r.email(),r.displayName(),r.password(),r.role());}
    @PatchMapping("/users/{userId}/status") @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public void status(@PathVariable UUID schoolId,@PathVariable UUID userId,@RequestParam boolean active,Authentication auth){access.requireSchool(auth,schoolId);service.userStatus(schoolId,access.currentUserId(auth),userId,active);}
    public record ProfileRequest(@NotBlank @Size(max=200) String schoolName,@NotBlank @Size(max=64) String timezone) {}
    public record SetupRequest(@NotBlank @Size(max=200) String schoolName,@NotBlank @Size(max=64) String timezone,
      @NotBlank @Pattern(regexp="[a-z0-9._-]{3,120}") String username,@NotBlank @Email @Size(max=254) String email,
      @NotBlank @Size(max=200) String displayName,@NotBlank @Size(min=12,max=128) String password){
      @Override public String toString(){return "SetupRequest[password=REDACTED]";}}
    public record UserRequest(@NotBlank @Pattern(regexp="[a-z0-9._-]{3,120}") String username,@NotBlank @Email @Size(max=254) String email,
      @NotBlank @Size(max=200) String displayName,@NotBlank @Size(min=12,max=128) String password,@NotBlank String role){
      @Override public String toString(){return "UserRequest[password=REDACTED]";}}
}
