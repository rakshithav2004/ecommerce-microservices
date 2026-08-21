package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Response containing details of an individual item in an order")
public record OrderItemResponse(

        @Schema(
                description = "Unique ID of the product",
                example = "68a1f2c34d1234567890abcd"
        )
        String productId,

        @Schema(
                description = "Name of the ordered product",
                example = "iPhone 15"
        )
        String productName,

        @Schema(
                description = "Unit price of the product",
                example = "69999.00"
        )
        BigDecimal price,

        @Schema(
                description = "Quantity of the product ordered",
                example = "2"
        )
        Integer quantity,

        @Schema(
                description = "Total price for this order item, calculated as price multiplied by quantity",
                example = "139998.00"
        )
        BigDecimal subtotal
) {
}