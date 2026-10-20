CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(120) NOT NULL,
    total_stock INT NOT NULL, available_stock INT NOT NULL, created_at DATETIME(6) NOT NULL,
    CONSTRAINT ck_product_stock CHECK (total_stock >= 0 AND available_stock >= 0 AND available_stock <= total_stock)
) ENGINE=InnoDB;
CREATE TABLE reservations (
    id BINARY(16) PRIMARY KEY, product_id BIGINT NOT NULL, customer_id VARCHAR(80) NOT NULL,
    quantity INT NOT NULL, idempotency_key VARCHAR(100) NOT NULL, status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL, expires_at DATETIME(6) NOT NULL, updated_at DATETIME(6),
    UNIQUE KEY uk_reservation_idempotency (idempotency_key), KEY idx_reservation_expiry (status, expires_at),
    CONSTRAINT fk_reservation_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;
CREATE TABLE outbox_events (
    id BINARY(16) PRIMARY KEY, event_type VARCHAR(60) NOT NULL, aggregate_type VARCHAR(60) NOT NULL,
    aggregate_id VARCHAR(80) NOT NULL, payload TEXT NOT NULL, created_at DATETIME(6) NOT NULL, published_at DATETIME(6),
    KEY idx_outbox_pending (published_at, created_at)
) ENGINE=InnoDB;

