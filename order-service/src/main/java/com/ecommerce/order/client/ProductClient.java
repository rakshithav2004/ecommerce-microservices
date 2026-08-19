package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "product-service",
        url = "${product-service.url}"
)
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ProductResponse getProductById(
            @PathVariable("id") String productId
    );

    @PutMapping("/api/v1/products/{id}/reserve-stock")
    ProductResponse reserveStock(
            @PathVariable("id") String productId,
            @RequestParam("quantity") int quantity
    );

    @PutMapping("/api/v1/products/{id}/release-stock")
    ProductResponse releaseStock(
            @PathVariable("id") String productId,
            @RequestParam("quantity") int quantity
    );
}