package com.ecommerce.order.dto;

import java.math.BigDecimal;

public record ProductResponse(

        String id,
        String sku,
        String name,
        String category,
        String description,
        BigDecimal price,
        Integer stock,
        Boolean active
) {
}