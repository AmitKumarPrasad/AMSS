package com.edusphere.ai;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/ai")
public class AiAssistantController {
    private final RagService ragService;
    private final SchoolAiAssistant assistant;
    private final TenantAccess tenantAccess;

    public AiAssistantController(RagService ragService, SchoolAiAssistant assistant, TenantAccess tenantAccess) {
        this.ragService = ragService;
        this.assistant = assistant;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping("/documents/{documentId}/index")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public void index(@PathVariable UUID schoolId, @PathVariable UUID documentId,
                      @Valid @RequestBody IndexDocumentRequest request,
                      Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        ragService.index(schoolId, documentId, request.content());
    }

    @PostMapping("/ask")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','PARENT','STUDENT')")
    public AssistantResponse ask(@PathVariable UUID schoolId,
                                 @Valid @RequestBody AskRequest request,
                                 Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        List<String> roles = authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        List<RagService.RagResult> evidence = ragService.search(schoolId, roles, request.question(), 8);
        return new AssistantResponse(assistant.answer(request.question(), evidence), evidence);
    }

    public record IndexDocumentRequest(@NotBlank String content) {}
    public record AskRequest(@NotBlank String question) {}
    public record AssistantResponse(String answer, List<RagService.RagResult> evidence) {}
}
