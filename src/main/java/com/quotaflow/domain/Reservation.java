package com.quotaflow.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations", uniqueConstraints = @UniqueConstraint(name = "uk_reservation_idempotency", columnNames = "idempotency_key"))
public class Reservation {
    public enum Status { PENDING, CONFIRMED, CANCELLED, EXPIRED }

    @Id
    private UUID id;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false, length = 80)
    private String customerId;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant expiresAt;
    private Instant updatedAt;

    protected Reservation() {}
    public Reservation(Long productId, String customerId, int quantity, String key, Instant expiresAt) {
        this.id = UUID.randomUUID(); this.productId = productId; this.customerId = customerId;
        this.quantity = quantity; this.idempotencyKey = key; this.status = Status.PENDING;
        this.createdAt = Instant.now(); this.updatedAt = this.createdAt; this.expiresAt = expiresAt;
    }
    public void confirm() { this.status = Status.CONFIRMED; this.updatedAt = Instant.now(); }
    public void cancel() { this.status = Status.CANCELLED; this.updatedAt = Instant.now(); }
    public void expire() { this.status = Status.EXPIRED; this.updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public Long getProductId() { return productId; }
    public String getCustomerId() { return customerId; }
    public int getQuantity() { return quantity; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

