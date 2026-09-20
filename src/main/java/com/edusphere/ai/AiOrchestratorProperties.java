package com.edusphere.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.ai.orchestrator")
public record AiOrchestratorProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout) {

    public AiOrchestratorProperties {
        baseUrl = baseUrl == null || baseUrl.isBlank() ? "http://localhost:8123" : baseUrl.trim();
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(30) : readTimeout;
    }
}
