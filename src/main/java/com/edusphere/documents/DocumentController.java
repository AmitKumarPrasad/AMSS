package com.edusphere.documents;

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
@RequestMapping("/api/v1/schools/{schoolId}/documents")
public class DocumentController {
    private final DocumentService documentService;
    private final TenantAccess tenantAccess;

    public DocumentController(DocumentService documentService, TenantAccess tenantAccess) {
        this.documentService = documentService; this.tenantAccess = tenantAccess;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public DocumentService.DocumentView register(@PathVariable UUID schoolId,
                                                   @Valid @RequestBody CreateDocumentRequest request,
                                                   Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return documentService.register(schoolId, new DocumentService.CreateDocumentRequest(
                request.title(), request.description(), request.fileName(), request.contentType(),
                request.storageKey(), request.checksum(), request.audienceRole()), tenantAccess.currentUserId(authentication));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','PARENT','STUDENT')")
    public List<DocumentService.DocumentView> list(@PathVariable UUID schoolId,
                                                    @RequestParam(defaultValue = "true") boolean publishedOnly,
                                                    Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return documentService.list(schoolId, publishedOnly);
    }

    @PostMapping("/{documentId}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public DocumentService.DocumentView publish(@PathVariable UUID schoolId, @PathVariable UUID documentId,
                                                 Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return documentService.publish(schoolId, documentId);
    }

    @PostMapping("/{documentId}/archive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public DocumentService.DocumentView archive(@PathVariable UUID schoolId, @PathVariable UUID documentId,
                                                 Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return documentService.archive(schoolId, documentId);
    }

    public record CreateDocumentRequest(@NotBlank String title, String description, @NotBlank String fileName,
                                        @NotBlank String contentType, @NotBlank String storageKey,
                                        String checksum, String audienceRole) {}
}
