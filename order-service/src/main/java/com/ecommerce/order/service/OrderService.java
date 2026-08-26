package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.PaymentStatus;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(String id);
    OrderResponse getOrderByNumber(String orderNumber);
    OrderResponse cancelOrder(String orderId);
    OrderResponse updateOrderStatus(
            String orderId,
            OrderStatus newStatus
    );
    OrderResponse updatePaymentStatus(
            String orderId,
            PaymentStatus paymentStatus
    );
}