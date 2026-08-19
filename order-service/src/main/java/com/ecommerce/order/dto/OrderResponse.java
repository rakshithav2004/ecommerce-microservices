package com.ecommerce.order.dto;

import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

        String id,

        String orderNumber,

        String customerId,

        List<OrderItemResponse> items,

        BigDecimal totalAmount,

        OrderStatus status,

        PaymentStatus paymentStatus,

        LocalDateTime createdAt
) {
}