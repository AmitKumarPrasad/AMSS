package com.edusphere.operations;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id")
    private UUID schoolId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "aggregate_type", length = 120)
    private String aggregateType;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload = "{}";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuditEvent() {}

    public AuditEvent(UUID schoolId, UUID actorUserId, String eventType,
                      String aggregateType, UUID aggregateId, String payload) {
        this.schoolId = schoolId;
        this.actorUserId = actorUserId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload == null || payload.isBlank() ? "{}" : payload;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getActorUserId() { return actorUserId; }
    public String getEventType() { return eventType; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
}
