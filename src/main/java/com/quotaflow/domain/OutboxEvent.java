package com.quotaflow.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", indexes = @Index(name = "idx_outbox_pending", columnList = "published_at,created_at"))
public class OutboxEvent {
    @Id private UUID id;
    @Column(nullable = false, length = 60) private String eventType;
    @Column(nullable = false, length = 60) private String aggregateType;
    @Column(nullable = false, length = 80) private String aggregateId;
    @Lob @Column(nullable = false) private String payload;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    private Instant publishedAt;

    protected OutboxEvent() {}
    public OutboxEvent(String eventType, String aggregateType, String aggregateId, String payload) {
        this.id = UUID.randomUUID(); this.eventType = eventType; this.aggregateType = aggregateType;
        this.aggregateId = aggregateId; this.payload = payload; this.createdAt = Instant.now();
    }
    public void markPublished() { this.publishedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
}

