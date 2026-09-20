package com.edusphere.ai;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiOrchestratorClientTest {

    @Test
    void propagatesTrustedContextAndCorrelationHeaders() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        UUID requestId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        server.createContext("/internal/v1/ai/ask", exchange -> {
            assertThat(exchange.getRequestHeaders().getFirst("X-Request-ID")).isEqualTo(requestId.toString());
            assertThat(exchange.getRequestHeaders().getFirst("X-School-ID")).isEqualTo(schoolId.toString());
            assertThat(exchange.getRequestHeaders().getFirst("X-User-ID")).isEqualTo(userId.toString());
            assertThat(exchange.getRequestHeaders().getFirst("X-User-Roles"))
                    .isEqualTo("ROLE_TEACHER,ROLE_SCHOOL_ADMIN");
            byte[] response = ("{\"requestId\":\"%s\",\"answer\":\"ok\",\"citations\":[\"doc-1\"],\"status\":\"COMPLETED\"}")
                    .formatted(requestId).getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();

        try {
            String baseUrl = "http://localhost:" + server.getAddress().getPort();
            AiOrchestratorClient client = new AiOrchestratorClient(
                    RestClientBuilderHolder.builder(),
                    new AiOrchestratorProperties(baseUrl, Duration.ofSeconds(1), Duration.ofSeconds(2)));

            var response = client.ask(
                    new AiOrchestratorClient.AiOrchestratorRequest(
                            requestId, schoolId, userId,
                            List.of("ROLE_TEACHER", "ROLE_SCHOOL_ADMIN"),
                            "What is the attendance policy?", "conv-1"),
                    requestId.toString());

            assertThat(response.answer()).isEqualTo("ok");
            assertThat(response.citations()).containsExactly("doc-1");
            assertThat(response.status()).isEqualTo("COMPLETED");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void mapsTransportFailureToStableOrchestratorException() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/internal/v1/ai/ask", exchange -> exchange.sendResponseHeaders(502, -1));
        server.start();

        try {
            String baseUrl = "http://localhost:" + server.getAddress().getPort();
            AiOrchestratorClient client = new AiOrchestratorClient(
                    RestClientBuilderHolder.builder(),
                    new AiOrchestratorProperties(baseUrl, Duration.ofSeconds(1), Duration.ofSeconds(2)));

            assertThatThrownBy(() -> client.ask(
                    new AiOrchestratorClient.AiOrchestratorRequest(
                            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                            List.of("ROLE_TEACHER"), "hello", null),
                    UUID.randomUUID().toString()))
                    .isInstanceOf(AiOrchestratorClient.AiOrchestratorException.class)
                    .hasMessage("AI orchestrator is unavailable");
        } finally {
            server.stop(0);
        }
    }

    private static final class RestClientBuilderHolder {
        private static org.springframework.web.client.RestClient.Builder builder() {
            return org.springframework.web.client.RestClient.builder();
        }
    }
}
