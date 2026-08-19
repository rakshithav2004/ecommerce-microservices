package com.ecommerce.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(

        String productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal
) {
}