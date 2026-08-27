package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Response containing complete product details")
public record ProductResponse(
    @Schema(description = "Unique identifier of the product", example = "68a1f2c34d1234567890abcd")
        String id,
    @Schema(description = "Unique stock keeping unit of the product", example = "PHONE-001")
        String sku,
    @Schema(description = "Name of the product", example = "iPhone 15") String name,
    @Schema(description = "Category to which the product belongs", example = "Electronics")
        String category,
    @Schema(
            description = "Detailed description of the product",
            example = "Apple iPhone 15 with 128GB storage")
        String description,
    @Schema(description = "Current price of the product", example = "69999.00") BigDecimal price,
    @Schema(description = "Current available stock quantity", example = "50") Integer stock,
    @Schema(description = "Indicates whether the product is active and available", example = "true")
        Boolean active) {}
