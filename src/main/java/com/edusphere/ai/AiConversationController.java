package com.edusphere.ai;

import com.edusphere.security.TenantAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools/{schoolId}/ai/conversations")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','PARENT','STUDENT')")
public class AiConversationController {
    private final AiConversationService conversations;
    private final TenantAccess tenantAccess;

    public AiConversationController(AiConversationService conversations, TenantAccess tenantAccess) {
        this.conversations = conversations;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping
    public AiConversationService.ConversationView create(
            @PathVariable UUID schoolId,
            @Valid @RequestBody CreateConversationRequest request,
            Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return conversations.create(schoolId, tenantAccess.currentUserId(authentication), request.title());
    }

    @GetMapping
    public List<AiConversationService.ConversationSummary> list(
            @PathVariable UUID schoolId,
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return conversations.list(schoolId, tenantAccess.currentUserId(authentication), limit);
    }

    @GetMapping("/{conversationId}")
    public AiConversationService.ConversationView get(
            @PathVariable UUID schoolId,
            @PathVariable UUID conversationId,
            Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        return conversations.get(schoolId, tenantAccess.currentUserId(authentication), conversationId);
    }

    @DeleteMapping("/{conversationId}")
    public void delete(
            @PathVariable UUID schoolId,
            @PathVariable UUID conversationId,
            Authentication authentication) {
        tenantAccess.requireSchool(authentication, schoolId);
        conversations.delete(schoolId, tenantAccess.currentUserId(authentication), conversationId);
    }

    public record CreateConversationRequest(@Size(max = 200) String title) {}
}
