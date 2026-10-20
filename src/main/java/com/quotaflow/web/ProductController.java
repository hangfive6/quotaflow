package com.quotaflow.web;

import com.quotaflow.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static com.quotaflow.web.ApiModels.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }
    @PostMapping public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(service.create(request.name(), request.stock())));
    }
    @GetMapping public List<ProductResponse> list() { return service.list().stream().map(ProductResponse::from).toList(); }
    @GetMapping("/{id}") public ProductResponse get(@PathVariable Long id) { return ProductResponse.from(service.get(id)); }
}

