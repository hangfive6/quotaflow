package com.quotaflow.web;

import com.quotaflow.domain.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class ApiModels {
    private ApiModels() {}
    public record CreateProductRequest(@NotBlank @Size(max=120) String name, @Min(1) @Max(1_000_000) int stock) {}
    public record ProductResponse(Long id, String name, int totalStock, int availableStock, Instant createdAt) {
        public static ProductResponse from(Product p) { return new ProductResponse(p.getId(), p.getName(), p.getTotalStock(), p.getAvailableStock(), p.getCreatedAt()); }
    }
    public record CreateReservationRequest(@NotNull Long productId, @NotBlank @Size(max=80) String customerId, @Min(1) @Max(100) int quantity) {}
    public record ReservationResponse(UUID id, Long productId, String customerId, int quantity, Reservation.Status status, Instant expiresAt, Instant createdAt) {
        public static ReservationResponse from(Reservation r) { return new ReservationResponse(r.getId(), r.getProductId(), r.getCustomerId(), r.getQuantity(), r.getStatus(), r.getExpiresAt(), r.getCreatedAt()); }
    }
    public record ErrorResponse(String code, String message, String requestId, Instant timestamp) {}
}

