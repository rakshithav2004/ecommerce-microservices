package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.PaymentStatus;
import java.util.List;

public interface OrderService {
  OrderResponse createOrder(OrderRequest request, String customerId);

  OrderResponse getOrderById(String id);

  OrderResponse getOrderByNumber(String orderNumber);

  OrderResponse cancelOrder(String orderId);

  OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus);

  OrderResponse updatePaymentStatus(String orderId, PaymentStatus paymentStatus);

  List<OrderResponse> getMyOrders(String customerId);

  List<OrderResponse> getAllOrders();
}
