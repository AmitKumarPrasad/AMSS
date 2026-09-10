package com.edusphere.mcp;

import com.edusphere.student.StudentService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SchoolTools {
    private final StudentService studentService;
    public SchoolTools(StudentService studentService) { this.studentService = studentService; }

    @McpTool(name = "list_active_students", description = "List active students for a school. Use only when the caller is authorized for that school.")
    public Object listActiveStudents(
            @McpToolParam(description = "School UUID", required = true) String schoolId) {
        return studentService.activeStudents(UUID.fromString(schoolId));
    }
}
