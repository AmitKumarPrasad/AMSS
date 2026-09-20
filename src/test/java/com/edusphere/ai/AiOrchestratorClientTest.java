package com.edusphere.ai;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiOrchestratorClientTest {

    @Test
    void propagatesTrustedContextAndCorrelationHeaders() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiOrchestratorClient client = new AiOrchestratorClient(builder.build());

        UUID requestId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        server.expect(requestTo("http://localhost:8123/internal/v1/ai/ask"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("X-Request-ID", requestId.toString()))
                .andExpect(header("X-School-ID", schoolId.toString()))
                .andExpect(header("X-User-ID", userId.toString()))
                .andExpect(header("X-User-Roles", "ROLE_TEACHER,ROLE_SCHOOL_ADMIN"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(
                        "{\"requestId\":\"%s\",\"answer\":\"ok\",\"citations\":[\"doc-1\"],\"status\":\"COMPLETED\"}"
                                .formatted(requestId),
                        MediaType.APPLICATION_JSON));

        var response = client.ask(
                new AiOrchestratorClient.AiOrchestratorRequest(
                        requestId, schoolId, userId,
                        List.of("ROLE_TEACHER", "ROLE_SCHOOL_ADMIN"),
                        "What is the attendance policy?", "conv-1"),
                requestId.toString());

        assertThat(response.answer()).isEqualTo("ok");
        assertThat(response.citations()).containsExactly("doc-1");
        assertThat(response.status()).isEqualTo("COMPLETED");
        server.verify();
    }

    @Test
    void mapsTransportFailureToStableOrchestratorException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiOrchestratorClient client = new AiOrchestratorClient(builder.build());

        server.expect(requestTo("http://localhost:8123/internal/v1/ai/ask"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.ask(
                new AiOrchestratorClient.AiOrchestratorRequest(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        List.of("ROLE_TEACHER"), "hello", null),
                UUID.randomUUID().toString()))
                .isInstanceOf(AiOrchestratorClient.AiOrchestratorException.class)
                .hasMessage("AI orchestrator is unavailable");
    }
}
