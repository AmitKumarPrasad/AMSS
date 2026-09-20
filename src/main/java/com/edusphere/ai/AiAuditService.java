package com.edusphere.ai;

import com.edusphere.operations.AuditEvent;
import com.edusphere.operations.AuditEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AiAuditService {
    private static final Logger log = LoggerFactory.getLogger(AiAuditService.class);
    private final AuditEventRepository repository;
    private final Counter auditFailureCounter;
    private final Counter requestCounter;
    private final Counter failureCounter;

    public AiAuditService(AuditEventRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
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

        String safeRequestId = requestId == null ? "" : requestId;
        String safeFailureType = failureType == null ? "" : failureType.replaceAll("[^A-Za-z0-9_.-]", "_");
        String payload = "{"request_id":"" + escape(safeRequestId)
                + "","operation":"" + escape(operation)
                + "","status":"" + escape(status)
                + "","latency_ms":" + Math.max(0, latencyMs)
                + ","evidence_count":" + Math.max(0, evidenceCount)
                + ","input_length":" + Math.max(0, inputLength)
                + ","output_length":" + Math.max(0, outputLength)
                + ","failure_type":"" + escape(safeFailureType) + ""}";

        try {
            repository.save(new AuditEvent(schoolId, userId, "AI_" + operation,
                    "AI_REQUEST", null, payload));
        } catch (RuntimeException ex) {
            auditFailureCounter.increment();
            log.warn("AI audit event could not be persisted for requestId={}", safeRequestId);
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace(""", "\\"");
    }
}
