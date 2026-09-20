package com.edusphere.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class AiOrchestratorClient {
    private final RestClient client;

    @Autowired
    @Autowired
    public AiOrchestratorClient(RestClient.Builder builder, AiOrchestratorProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.readTimeout());
        this.client = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }

    AiOrchestratorClient(RestClient client) {
        this.client = client;
    }

    public AiOrchestratorResponse ask(AiOrchestratorRequest request, String requestId) {
        try {
            return client.post()
                    .uri("/internal/v1/ai/ask")
                    .header("Authorization", "Bearer " + serviceToken())
                    .header("X-Request-ID", requestId)
                    .header("X-School-ID", request.schoolId().toString())
                    .header("X-User-ID", request.userId().toString())
                    .header("X-User-Roles", String.join(",", request.roles()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiOrchestratorResponse.class);
        } catch (RestClientException ex) {
            throw new AiOrchestratorException("AI orchestrator is unavailable", ex);
        }
    }

    private String serviceToken() {
        return org.springframework.util.StringUtils.hasText(System.getenv("AI_ORCHESTRATOR_SERVICE_TOKEN"))
                ? System.getenv("AI_ORCHESTRATOR_SERVICE_TOKEN")
                : "";
    }

    public record AiOrchestratorRequest(
            UUID requestId,
            UUID schoolId,
            UUID userId,
            List<String> roles,
            String message,
            String conversationId) {
    }

    public record AiOrchestratorResponse(
            UUID requestId,
            String answer,
            List<String> citations,
            String status) {
    }

    public static class AiOrchestratorException extends RuntimeException {
        public AiOrchestratorException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
