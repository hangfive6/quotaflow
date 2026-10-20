package com.quotaflow.service;

import com.quotaflow.domain.Product;
import com.quotaflow.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository) { this.repository = repository; }
    @Transactional public Product create(String name, int stock) { return repository.save(new Product(name, stock)); }
    @Transactional(readOnly = true) public List<Product> list() { return repository.findAll(); }
    @Transactional(readOnly = true) public Product get(Long id) { return repository.findById(id).orElseThrow(() -> new NotFoundException("Product not found: " + id)); }
}

