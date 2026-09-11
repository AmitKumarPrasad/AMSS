package com.edusphere.communication;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/communications")
public class CommunicationController {
    private final CommunicationService service;
    private final TenantAccess tenantAccess;
    public CommunicationController(CommunicationService service,TenantAccess tenantAccess){this.service=service;this.tenantAccess=tenantAccess;}

    @PostMapping("/announcements")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public CommunicationService.AnnouncementView create(@PathVariable UUID schoolId,@Valid @RequestBody CreateAnnouncementRequest request,Authentication authentication){
        tenantAccess.requireSchool(authentication,schoolId);
        return service.create(schoolId,request.title(),request.body(),request.audienceRole(),tenantAccess.currentUserId(authentication));
    }

    @PostMapping("/announcements/{announcementId}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public CommunicationService.AnnouncementView publish(@PathVariable UUID schoolId,@PathVariable UUID announcementId,Authentication authentication){
        tenantAccess.requireSchool(authentication,schoolId); return service.publish(schoolId,announcementId);
    }

    @GetMapping("/announcements")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','PARENT','STUDENT')")
    public List<CommunicationService.AnnouncementView> list(@PathVariable UUID schoolId,@RequestParam(defaultValue="true") boolean publishedOnly,Authentication authentication){
        tenantAccess.requireSchool(authentication,schoolId);
        return service.list(schoolId,publishedOnly,authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }

    @PostMapping("/announcements/{announcementId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','PARENT','STUDENT')")
    public void markRead(@PathVariable UUID schoolId,@PathVariable UUID announcementId,Authentication authentication){
        tenantAccess.requireSchool(authentication,schoolId); service.markRead(schoolId,announcementId,tenantAccess.currentUserId(authentication));
    }

    public record CreateAnnouncementRequest(@NotBlank String title,@NotBlank String body,String audienceRole){}
}
