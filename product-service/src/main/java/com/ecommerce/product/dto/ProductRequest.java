package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload used to create or update a product")
public record ProductRequest(

        @Schema(
                description = "Unique stock keeping unit of the product",
                example = "PHONE-001",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "SKU is required")
        String sku,

        @Schema(
                description = "Name of the product",
                example = "iPhone 15",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Product name is required")
        String name,

        @Schema(
                description = "Category to which the product belongs",
                example = "Electronics",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Category is required")
        String category,

        @Schema(
                description = "Detailed description of the product",
                example = "Apple iPhone 15 with 128GB storage"
        )
        String description,

        @Schema(
                description = "Price of the product",
                example = "69999.00",
                minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @Schema(
                description = "Initial available stock quantity",
                example = "50",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock
) {
}