package com.quotaflow.repository;

import com.quotaflow.domain.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.availableStock = p.availableStock - :quantity where p.id = :id and p.availableStock >= :quantity")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.availableStock = p.availableStock + :quantity where p.id = :id")
    int restoreStock(@Param("id") Long id, @Param("quantity") int quantity);
}

