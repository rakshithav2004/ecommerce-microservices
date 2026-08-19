package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

    @GetMapping
    public Page<ProductResponse> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            Pageable pageable) {

        return productService.getAllProducts(
                category,
                search,
                minPrice,
                maxPrice,
                inStock,
                pageable
        );
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }

    @PutMapping("/{id}/reserve-stock")
    public ProductResponse reserveStock(
            @PathVariable String id,
            @RequestParam int quantity) {
        return productService.reserveStock(id, quantity);
    }

    @PutMapping("/{id}/release-stock")
    public ProductResponse releaseStock(
            @PathVariable String id,
            @RequestParam int quantity) {

        return productService.releaseStock(id, quantity);
    }
}