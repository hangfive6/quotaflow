package com.quotaflow.web;

import com.quotaflow.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import static com.quotaflow.web.ApiModels.*;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
    private final ReservationService service;
    public ReservationController(ReservationService service) { this.service = service; }
    @PostMapping public ResponseEntity<ReservationResponse> create(
            @RequestHeader("X-Idempotency-Key") String key, @Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(service.create(request.productId(), request.customerId(), request.quantity(), key)));
    }
    @GetMapping("/{id}") public ReservationResponse get(@PathVariable UUID id) { return ReservationResponse.from(service.get(id)); }
    @PostMapping("/{id}/confirm") public ReservationResponse confirm(@PathVariable UUID id) { return ReservationResponse.from(service.confirm(id)); }
    @DeleteMapping("/{id}") public ReservationResponse cancel(@PathVariable UUID id) { return ReservationResponse.from(service.cancel(id)); }
}

