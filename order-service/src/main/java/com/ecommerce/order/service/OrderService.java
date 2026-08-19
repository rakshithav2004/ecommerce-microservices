package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.OrderStatus;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(String id);
    OrderResponse getOrderByNumber(String orderNumber);
    OrderResponse cancelOrder(String orderId);
    OrderResponse updateOrderStatus(
            String orderId,
            OrderStatus newStatus
    );
}