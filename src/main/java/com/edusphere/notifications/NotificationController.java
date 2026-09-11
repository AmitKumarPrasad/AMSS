package com.edusphere.notifications;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/notifications")
public class NotificationController {
    private final NotificationService service;
    private final TenantAccess tenantAccess;

    public NotificationController(NotificationService service, TenantAccess tenantAccess) {
        this.service = service;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public NotificationService.NotificationView enqueue(@PathVariable UUID schoolId,
                                                        @Valid @RequestBody CreateNotification request,
                                                        Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.enqueue(schoolId, request.recipientUserId(), request.channel(), request.subject(), request.body());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationService.NotificationView> list(@PathVariable UUID schoolId, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.list(schoolId, tenantAccess.currentUserId(authentication));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public long unreadCount(@PathVariable UUID schoolId, Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.unreadCount(schoolId, tenantAccess.currentUserId(authentication));
    }

    @PostMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public NotificationService.NotificationView markRead(@PathVariable UUID schoolId,
                                                         @PathVariable UUID notificationId,
                                                         Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return service.markRead(schoolId, notificationId, tenantAccess.currentUserId(authentication));
    }

    public record CreateNotification(@NotNull UUID recipientUserId, String channel, String subject, @NotBlank String body) {}
}
