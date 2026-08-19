package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    Page<Product> findByCategoryIgnoreCase(
            String category,
            Pageable pageable
    );

    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name,
            String description,
            Pageable pageable
    );
}