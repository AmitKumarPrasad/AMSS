package com.edusphere.student;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/students")
public class StudentController {
    private final StudentService service;
    public StudentController(StudentService service) { this.service = service; }

    @GetMapping
    public List<StudentService.StudentView> list(@PathVariable @NotNull UUID schoolId) {
        return service.activeStudents(schoolId);
    }
}
