package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product with the provided details and validates the product request"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product using its unique product ID"
    )
    @GetMapping("/{id}")
    public ProductResponse getProductById(
            @PathVariable String id) {
        return productService.getProductById(id);
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieves a paginated list of products with optional filtering by category, search keyword, price range, and stock availability"
    )
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

    @Operation(
            summary = "Update a product",
            description = "Updates the details of an existing product using its unique product ID"
    )
    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @Operation(
            summary = "Delete a product",
            description = "Deletes an existing product using its unique product ID"
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }

    @Operation(
            summary = "Reserve product stock",
            description = "Reserves the specified quantity of stock for a product when processing an order"
    )
    @PutMapping("/{id}/reserve-stock")
    public ProductResponse reserveStock(
            @PathVariable String id,
            @RequestParam int quantity) {
        return productService.reserveStock(id, quantity);
    }

    @Operation(
            summary = "Release product stock",
            description = "Releases previously reserved stock for a product when an order is cancelled"
    )
    @PutMapping("/{id}/release-stock")
    public ProductResponse releaseStock(
            @PathVariable String id,
            @RequestParam int quantity) {
        return productService.releaseStock(id, quantity);
    }
}