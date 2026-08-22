package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceClient {

    private final ProductClient productClient;

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "getProductByIdFallback"
    )
    public ProductResponse getProductById(String productId) {
        return productClient.getProductById(productId);
    }

    public ProductResponse getProductByIdFallback(
            String productId,
            Throwable throwable) {
        throw new IllegalStateException(
                "Product Service is currently unavailable. " +
                        "Please try again later."
        );
    }

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "reserveStockFallback"
    )
    public ProductResponse reserveStock(
            String productId,
            int quantity) {
        return productClient.reserveStock(productId, quantity);
    }

    public ProductResponse reserveStockFallback(
            String productId,
            int quantity,
            Throwable throwable) {
        throw new IllegalStateException(
                "Product Service is currently unavailable. " +
                        "Unable to reserve stock."
        );
    }

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "releaseStockFallback"
    )
    public ProductResponse releaseStock(
            String productId,
            int quantity) {
        return productClient.releaseStock(productId, quantity);
    }

    public ProductResponse releaseStockFallback(
            String productId,
            int quantity,
            Throwable throwable) {
        throw new IllegalStateException(
                "Product Service is currently unavailable. " +
                        "Unable to release stock."
        );
    }
}

