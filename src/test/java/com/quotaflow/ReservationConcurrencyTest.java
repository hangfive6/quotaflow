package com.quotaflow;

import com.quotaflow.domain.Product;
import com.quotaflow.repository.ProductRepository;
import com.quotaflow.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"app.rate-limit-per-second=1000", "app.expiration-scan-ms=3600000"})
class ReservationConcurrencyTest {
    @Autowired ProductService productService;
    @Autowired ReservationService reservationService;
    @Autowired ProductRepository productRepository;

    @Test void neverOversellsUnderConcurrentRequests() throws Exception {
        Product product = productService.create("Limited keyboard", 10);
        int requests = 30;
        var pool = Executors.newFixedThreadPool(requests);
        var ready = new CountDownLatch(requests);
        var start = new CountDownLatch(1);
        var success = new AtomicInteger();
        var futures = new java.util.ArrayList<Future<?>>();
        for (int i = 0; i < requests; i++) {
            int n = i;
            futures.add(pool.submit(() -> {
                ready.countDown(); start.await();
                try { reservationService.create(product.getId(), "user-" + n, 1, "key-" + n); success.incrementAndGet(); }
                catch (BusinessException ignored) {}
                return null;
            }));
        }
        ready.await(); start.countDown();
        for (var future : futures) future.get();
        pool.shutdown();
        assertThat(success.get()).isEqualTo(10);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getAvailableStock()).isZero();
    }
}

