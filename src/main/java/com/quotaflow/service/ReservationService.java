package com.quotaflow.service;

import com.quotaflow.domain.*;
import com.quotaflow.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class ReservationService {
    private final ProductRepository products;
    private final ReservationRepository reservations;
    private final OutboxEventRepository outbox;
    private final Duration ttl;

    public ReservationService(ProductRepository products, ReservationRepository reservations, OutboxEventRepository outbox,
                              @Value("${app.reservation-ttl:PT15M}") Duration ttl) {
        this.products = products; this.reservations = reservations; this.outbox = outbox; this.ttl = ttl;
    }

    @Transactional
    public Reservation create(Long productId, String customerId, int quantity, String key) {
        var existing = reservations.findByIdempotencyKey(key);
        if (existing.isPresent()) return existing.get();
        if (!products.existsById(productId)) throw new NotFoundException("Product not found: " + productId);
        if (products.deductStock(productId, quantity) != 1) throw new BusinessException("INSUFFICIENT_STOCK", "Not enough stock");
        var reservation = new Reservation(productId, customerId, quantity, key, Instant.now().plus(ttl));
        try {
            reservation = reservations.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("DUPLICATE_REQUEST", "The idempotency key is already in use");
        }
        appendEvent("RESERVATION_CREATED", reservation);
        return reservation;
    }

    @Transactional(readOnly = true)
    public Reservation get(UUID id) { return reservations.findById(id).orElseThrow(() -> new NotFoundException("Reservation not found: " + id)); }

    @Transactional
    public Reservation confirm(UUID id) {
        var r = lock(id);
        if (r.getStatus() == Reservation.Status.CONFIRMED) return r;
        if (r.getStatus() != Reservation.Status.PENDING || r.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("INVALID_STATE", "Only a non-expired pending reservation can be confirmed");
        }
        r.confirm(); appendEvent("RESERVATION_CONFIRMED", r); return r;
    }

    @Transactional
    public Reservation cancel(UUID id) {
        var r = lock(id);
        if (r.getStatus() == Reservation.Status.CANCELLED) return r;
        if (r.getStatus() != Reservation.Status.PENDING) throw new BusinessException("INVALID_STATE", "Only a pending reservation can be cancelled");
        products.restoreStock(r.getProductId(), r.getQuantity());
        r.cancel(); appendEvent("RESERVATION_CANCELLED", r); return r;
    }

    private Reservation lock(UUID id) { return reservations.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Reservation not found: " + id)); }
    private void appendEvent(String type, Reservation r) {
        outbox.save(new OutboxEvent(type, "Reservation", r.getId().toString(), "{\"reservationId\":\"" + r.getId() + "\",\"status\":\"" + r.getStatus() + "\"}"));
    }
}

