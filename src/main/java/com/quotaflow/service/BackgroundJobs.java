package com.quotaflow.service;

import com.quotaflow.domain.*;
import com.quotaflow.repository.*;
import org.slf4j.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
public class BackgroundJobs {
    private static final Logger log = LoggerFactory.getLogger(BackgroundJobs.class);
    private final ReservationRepository reservations;
    private final ProductRepository products;
    private final OutboxEventRepository outbox;
    public BackgroundJobs(ReservationRepository reservations, ProductRepository products, OutboxEventRepository outbox) {
        this.reservations = reservations; this.products = products; this.outbox = outbox;
    }

    @Scheduled(fixedDelayString = "${app.expiration-scan-ms:30000}")
    @Transactional
    public void releaseExpiredReservations() {
        for (var r : reservations.findExpiredPendingForUpdate(Instant.now())) {
            products.restoreStock(r.getProductId(), r.getQuantity());
            r.expire();
            outbox.save(new OutboxEvent("RESERVATION_EXPIRED", "Reservation", r.getId().toString(), "{\"reservationId\":\"" + r.getId() + "\"}"));
        }
    }

    @Scheduled(fixedDelayString = "${app.outbox-publish-ms:5000}")
    @Transactional
    public void publishOutbox() {
        for (var event : outbox.findByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, 100))) {
            log.info("outbox event published type={} aggregateId={} payload={}", event.getEventType(), event.getAggregateId(), event.getPayload());
            event.markPublished();
        }
    }
}

