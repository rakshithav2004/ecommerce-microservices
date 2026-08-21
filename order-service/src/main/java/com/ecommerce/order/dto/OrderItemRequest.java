package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request containing product and quantity details for an order item")
public record OrderItemRequest(

        @Schema(
                description = "Unique ID of the product to be ordered",
                example = "68a1f2c34d1234567890abcd",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Product ID is required")
        String productId,

        @Schema(
                description = "Quantity of the product to order",
                example = "2",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}