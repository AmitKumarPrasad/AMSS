package com.edusphere.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
public class AiRequestIdempotencyService {
    private static final int STALE_MINUTES = 10;

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public AiRequestIdempotencyService(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Claim claim(UUID schoolId, UUID userId, UUID requestId) {
        int inserted = jdbc.update("""
                INSERT INTO ai_request_idempotency(school_id, user_id, request_id, status)
                VALUES (?, ?, ?, 'PROCESSING')
                ON CONFLICT (school_id, user_id, request_id) DO NOTHING
                """, schoolId, userId, requestId);

        if (inserted == 1) {
            return Claim.newRequest();
        }

        List<StoredRequest> rows = jdbc.query("""
                SELECT status, answer, evidence_json, conversation_id, updated_at
                FROM ai_request_idempotency
                WHERE school_id = ? AND user_id = ? AND request_id = ?
                """, (rs, row) -> new StoredRequest(
                rs.getString("status"),
                rs.getString("answer"),
                rs.getString("evidence_json"),
                rs.getObject("conversation_id", UUID.class),
                rs.getObject("updated_at", OffsetDateTime.class)
        ), schoolId, userId, requestId);

        if (rows.isEmpty()) {
            throw new IllegalStateException("AI idempotency record disappeared");
        }

        StoredRequest existing = rows.get(0);
        if ("COMPLETED".equals(existing.status())) {
            return Claim.completed(existing);
        }

        if (existing.updatedAt() != null && existing.updatedAt().isBefore(OffsetDateTime.now().minusMinutes(STALE_MINUTES))) {
            int reclaimed = jdbc.update("""
                    UPDATE ai_request_idempotency
                    SET updated_at = now()
                    WHERE school_id = ? AND user_id = ? AND request_id = ?
                      AND status = 'PROCESSING'
                      AND updated_at < now() - (? * interval '1 minute')
                    """, schoolId, userId, requestId, STALE_MINUTES);
            if (reclaimed == 1) {
                return Claim.newRequest();
            }
        }

        throw new ResponseStatusException(CONFLICT, "request with this X-Request-ID is already being processed");
    }

    @Transactional
    public void complete(UUID schoolId, UUID userId, UUID requestId,
                         String answer, List<RagService.RagResult> evidence, UUID conversationId) {
        final String evidenceJson;
        try {
            evidenceJson = objectMapper.writeValueAsString(evidence);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize AI evidence", ex);
        }

        jdbc.update("""
                UPDATE ai_request_idempotency
                SET status = 'COMPLETED',
                    answer = ?,
                    evidence_json = ?,
                    conversation_id = ?,
                    updated_at = now()
                WHERE school_id = ? AND user_id = ? AND request_id = ?
                """, answer, evidenceJson, conversationId, schoolId, userId, requestId);
    }

    public CachedResponse response(Claim claim) {
        if (!claim.completed()) {
            throw new IllegalArgumentException("claim is not completed");
        }
        try {
            List<RagService.RagResult> evidence = objectMapper.readValue(
                    claim.stored().evidenceJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RagService.RagResult.class));
            return new CachedResponse(claim.stored().answer(), evidence, claim.stored().conversationId());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to deserialize cached AI response", ex);
        }
    }

    public record Claim(boolean completed, StoredRequest stored) {
        static Claim newRequest() {
            return new Claim(false, null);
        }

        static Claim completed(StoredRequest stored) {
            return new Claim(true, stored);
        }
    }

    private record StoredRequest(String status, String answer, String evidenceJson,
                                 UUID conversationId, OffsetDateTime updatedAt) {}

    public record CachedResponse(String answer, List<RagService.RagResult> evidence, UUID conversationId) {}
}
