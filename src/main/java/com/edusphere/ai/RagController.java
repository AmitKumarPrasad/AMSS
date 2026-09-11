package com.edusphere.ai;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/ai/rag")
public class RagController {
    private final RagService service;
    private final SchoolAiAssistant assistant;
    private final TenantAccess tenantAccess;

    public RagController(RagService service, SchoolAiAssistant assistant, TenantAccess tenantAccess) {
        this.service = service;
        this.assistant = assistant;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping("/query")
    public Response query(@PathVariable UUID schoolId, @Valid @RequestBody Request request,
                          Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();
        List<RagService.RagResult> evidence = service.search(schoolId, roles, request.question(), 8);
        return new Response(assistant.answer(request.question(), evidence), evidence);
    }

    public record Request(@NotBlank String question) {}
    public record Response(String answer, List<RagService.RagResult> evidence) {}
}
