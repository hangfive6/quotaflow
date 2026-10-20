package com.quotaflow.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false)
    private int totalStock;
    @Column(nullable = false)
    private int availableStock;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Product() {}
    public Product(String name, int stock) { this.name = name; this.totalStock = stock; this.availableStock = stock; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getTotalStock() { return totalStock; }
    public int getAvailableStock() { return availableStock; }
    public Instant getCreatedAt() { return createdAt; }
}

