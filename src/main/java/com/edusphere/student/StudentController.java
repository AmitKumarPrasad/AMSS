package com.edusphere.student;

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
@RequestMapping("/api/v1/schools/{schoolId}/students")
public class StudentController {
    private final StudentService service;
    private final TenantAccess tenantAccess;

    public StudentController(StudentService service, TenantAccess tenantAccess) {
        this.service = service;
        this.tenantAccess = tenantAccess;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public List<StudentService.StudentView> list(@PathVariable @NotNull UUID schoolId,
                                                  Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.activeStudents(schoolId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public StudentService.StudentView create(@PathVariable @NotNull UUID schoolId,
                                              @Valid @RequestBody CreateStudentRequest request,
                                              Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.create(schoolId, new StudentService.CreateStudentRequest(
                request.admissionNumber(), request.firstName(), request.lastName(), request.dateOfBirth()));
    }

    public record CreateStudentRequest(
            @NotBlank String admissionNumber,
            @NotBlank String firstName,
            String lastName,
            LocalDate dateOfBirth) {}
}
