package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request used to create a new customer order")
public record OrderRequest(

        @Schema(
                description = "Unique ID of the customer placing the order",
                example = "CUST-001",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Customer ID is required")
        String customerId,

        @Schema(
                description = "List of products and quantities included in the order",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items
) {
}