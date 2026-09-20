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
    private final AiAuditService aiAuditService;

    public AiAssistantController(RagService ragService, SchoolAiAssistant assistant,
                                 TenantAccess tenantAccess, AiAuditService aiAuditService) {
        this.ragService = ragService;
        this.assistant = assistant;
        this.tenantAccess = tenantAccess;
        this.aiAuditService = aiAuditService;
    }

    @PostMapping("/documents/{documentId}/index")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public void index(@PathVariable UUID schoolId, @PathVariable UUID documentId,
                      @Valid @RequestBody IndexDocumentRequest request,
                      Authentication authentication,
                      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        tenantAccess.requireSchool(authentication, schoolId);
        long started = System.nanoTime();
        try {
            ragService.index(schoolId, documentId, request.content());
            aiAuditService.record(schoolId, tenantAccess.currentUserId(authentication), requestId,
                    "RAG_INDEX", "SUCCESS", elapsedMs(started), 0, request.content().length(), 0, null);
        } catch (RuntimeException ex) {
            aiAuditService.record(schoolId, tenantAccess.currentUserId(authentication), requestId,
                    "RAG_INDEX", "ERROR", elapsedMs(started), 0, request.content().length(), 0,
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }

    @PostMapping("/ask")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','PARENT','STUDENT')")
    public AssistantResponse ask(@PathVariable UUID schoolId,
                                 @Valid @RequestBody AskRequest request,
                                 Authentication authentication,
                                 @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        tenantAccess.requireSchool(authentication, schoolId);
        String question = request.question().trim();
        if (question.length() > 2000) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "question must not exceed 2000 characters");
        }
        long started = System.nanoTime();
        try {
            List<String> roles = authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList();
            List<RagService.RagResult> evidence = ragService.search(schoolId, roles, question, 8);
            String answer = assistant.answer(question, evidence);
            aiAuditService.record(schoolId, tenantAccess.currentUserId(authentication), requestId,
                    "ASK", "SUCCESS", elapsedMs(started), evidence.size(), question.length(),
                    answer == null ? 0 : answer.length(), null);
            return new AssistantResponse(answer, evidence);
        } catch (RuntimeException ex) {
            aiAuditService.record(schoolId, tenantAccess.currentUserId(authentication), requestId,
                    "ASK", "ERROR", elapsedMs(started), 0, question.length(), 0,
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private long elapsedMs(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    public record IndexDocumentRequest(@NotBlank String content) {}
    public record AskRequest(@NotBlank String question) {}
    public record AssistantResponse(String answer, List<RagService.RagResult> evidence) {}
}
