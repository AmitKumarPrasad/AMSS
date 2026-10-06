package com.edusphere.mcp;

import com.edusphere.student.StudentService;
import com.edusphere.notifications.NotificationService;
import com.edusphere.documents.DocumentService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SchoolTools {
    private final StudentService studentService;
    private final NotificationService notificationService;
    private final DocumentService documentService;

    public SchoolTools(StudentService studentService, NotificationService notificationService, DocumentService documentService) {
        this.studentService = studentService;
        this.notificationService = notificationService;
        this.documentService = documentService;
    }

    @McpTool(name = "list_active_students", description = "List active students for a school. Use only when the caller is authorized for that school.")
    public Object listActiveStudents(
            @McpToolParam(description = "School UUID", required = true) String schoolId) {
        return studentService.activeStudents(UUID.fromString(schoolId));
    }
    @McpTool(name = "get_unread_notification_count", description = "Get unread notification count for the authenticated school user. Tenant and user identifiers must come from trusted request context.")
    public long getUnreadNotificationCount(
            @McpToolParam(description = "School UUID from trusted context", required = true) String schoolId,
            @McpToolParam(description = "User UUID from trusted context", required = true) String userId) {
        return notificationService.unreadCount(UUID.fromString(schoolId), UUID.fromString(userId));
    }

    @McpTool(name = "list_published_documents", description = "List published school documents visible to the caller roles. Tenant and roles must come from trusted request context.")
    public Object listPublishedDocuments(
            @McpToolParam(description = "School UUID from trusted context", required = true) String schoolId,
            @McpToolParam(description = "Comma-separated roles from trusted context", required = true) String roles) {
        var authorities = java.util.Arrays.stream(roles.split(","))
                .map(String::trim).filter(value -> !value.isBlank()).toList();
        return documentService.list(UUID.fromString(schoolId), true, authorities, 50);
    }
}
