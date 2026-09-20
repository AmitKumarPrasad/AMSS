package com.edusphere.ai;

import com.edusphere.operations.AuditEvent;
import com.edusphere.operations.AuditEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AiAuditService {
    private static final Logger log = LoggerFactory.getLogger(AiAuditService.class);
    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;
    private final Counter auditFailureCounter;
    private final Counter requestCounter;
    private final Counter failureCounter;

    public AiAuditService(AuditEventRepository repository, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.auditFailureCounter = meterRegistry.counter("ai.audit.write.failures");
        this.requestCounter = meterRegistry.counter("ai.requests.total");
        this.failureCounter = meterRegistry.counter("ai.requests.failures");
    }

    public void record(UUID schoolId, UUID userId, String requestId, String operation,
                       String status, long latencyMs, int evidenceCount,
                       int inputLength, int outputLength, String failureType) {
        requestCounter.increment();
        if ("ERROR".equals(status)) {
            failureCounter.increment();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("request_id", requestId == null ? "" : requestId);
        payload.put("operation", operation);
        payload.put("status", status);
        payload.put("latency_ms", Math.max(0, latencyMs));
        payload.put("evidence_count", Math.max(0, evidenceCount));
        payload.put("input_length", Math.max(0, inputLength));
        payload.put("output_length", Math.max(0, outputLength));
        payload.put("failure_type", failureType == null ? "" : failureType);

        try {
            String json = objectMapper.writeValueAsString(payload);
            repository.save(new AuditEvent(schoolId, userId, "AI_" + operation,
                    "AI_REQUEST", null, json));
        } catch (JsonProcessingException ex) {
            auditFailureCounter.increment();
            log.warn("AI audit payload could not be serialized for requestId={}", requestId);
        } catch (RuntimeException ex) {
            auditFailureCounter.increment();
            log.warn("AI audit event could not be persisted for requestId={}", requestId);
        }
    }
}
