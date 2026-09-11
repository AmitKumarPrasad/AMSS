package com.edusphere.documents;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public DocumentService.DocumentView upload(@PathVariable UUID schoolId,
                                                @RequestPart("file") MultipartFile file,
                                                @RequestParam String title,
                                                @RequestParam(required = false) String description,
                                                @RequestParam(required = false) String audienceRole,
                                                Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        if (title == null || title.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
        }
        return documentService.upload(schoolId, file, title, description, audienceRole,
                tenantAccess.currentUserId(authentication));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','PARENT','STUDENT')")
    public List<DocumentService.DocumentView> list(@PathVariable UUID schoolId,
                                                    @RequestParam(defaultValue = "true") boolean publishedOnly,
                                                    Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return documentService.list(schoolId, publishedOnly,
                authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }

    @GetMapping("/{documentId}/content")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','PARENT','STUDENT')")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID schoolId, @PathVariable UUID documentId,
                                                         Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        DocumentService.DocumentDownload document = documentService.download(schoolId, documentId,
                authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
        return ResponseEntity.ok()
                .contentType(parseMediaType(document.contentType()))
                .contentLength(document.size())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.fileName().replace("\"", "") + "\"")
                .body(new InputStreamResource(document.content()));
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

    private MediaType parseMediaType(String contentType) {
        try { return MediaType.parseMediaType(contentType); }
        catch (IllegalArgumentException e) { return MediaType.APPLICATION_OCTET_STREAM; }
    }

    public record CreateDocumentRequest(@NotBlank String title, String description, @NotBlank String fileName,
                                        @NotBlank String contentType, @NotBlank String storageKey,
                                        String checksum, String audienceRole) {}
}
