package com.edusphere.operations;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AuditEvent record(UUID schoolId, UUID actorUserId, String eventType,
                             String aggregateType, UUID aggregateId, String payload) {
        if (eventType == null || eventType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventType is required");
        }
        return repository.save(new AuditEvent(schoolId, actorUserId, eventType.trim(),
                aggregateType, aggregateId, payload));
    }

    @Transactional(readOnly = true)
    public List<AuditView> recent(UUID schoolId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return repository.findBySchoolIdOrderByCreatedAtDesc(schoolId, PageRequest.of(0, safeLimit))
                .stream()
                .map(event -> new AuditView(event.getId(), event.getSchoolId(), event.getActorUserId(),
                        event.getEventType(), event.getAggregateType(), event.getAggregateId(),
                        event.getPayload(), event.getCreatedAt()))
                .toList();
    }

    public record AuditView(UUID id, UUID schoolId, UUID actorUserId, String eventType,
                            String aggregateType, UUID aggregateId, String payload, java.time.Instant createdAt) {}
}
