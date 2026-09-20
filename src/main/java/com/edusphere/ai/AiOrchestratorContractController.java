package com.edusphere.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/ai")
public class AiOrchestratorContractController {
    private final AiOrchestratorClient client;

    public AiOrchestratorContractController(AiOrchestratorClient client) {
        this.client = client;
    }

    @PostMapping("/ask")
    @ResponseStatus(HttpStatus.OK)
    public AiOrchestratorClient.AiOrchestratorResponse ask(
            @Valid @RequestBody AiOrchestratorRequest request,
            @RequestHeader("X-Request-ID") String requestId) {
        return client.ask(
                new AiOrchestratorClient.AiOrchestratorRequest(
                        request.requestId(),
                        request.schoolId(),
                        request.userId(),
                        request.roles(),
                        request.message().trim(),
                        request.conversationId()),
                requestId);
    }

    public record AiOrchestratorRequest(
            UUID requestId,
            UUID schoolId,
            UUID userId,
            List<@NotBlank String> roles,
            @NotBlank @Size(max = 2000) String message,
            @Size(max = 128) String conversationId) {
    }
}
