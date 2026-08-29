package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.exception.DuplicateOrderItemException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.PaymentStatus;
import com.ecommerce.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final ProductServiceClient productServiceClient;

  @Override
  public OrderResponse createOrder(OrderRequest request, String customerId) {

    Set<String> productIds = new HashSet<>();

    for (OrderItemRequest itemRequest : request.items()) {
      if (!productIds.add(itemRequest.productId())) {
        throw new DuplicateOrderItemException(
            "Product cannot appear more than once in an order: " + itemRequest.productId());
      }
    }

    List<OrderItem> orderItems = new ArrayList<>();
    List<OrderItem> reservedItems = new ArrayList<>();
    BigDecimal totalAmount = BigDecimal.ZERO;

    try {

      for (OrderItemRequest itemRequest : request.items()) {

        ProductResponse product = productServiceClient.getProductById(itemRequest.productId());

        productServiceClient.reserveStock(itemRequest.productId(), itemRequest.quantity());

        BigDecimal subtotal = product.price().multiply(BigDecimal.valueOf(itemRequest.quantity()));

        OrderItem orderItem =
            new OrderItem(
                product.id(), product.name(), product.price(), itemRequest.quantity(), subtotal);

        orderItems.add(orderItem);
        reservedItems.add(orderItem);

        totalAmount = totalAmount.add(subtotal);
      }

    } catch (Exception exception) {

      for (OrderItem item : reservedItems) {
        try {
          productServiceClient.releaseStock(item.getProductId(), item.getQuantity());

        } catch (Exception rollbackException) {
          log.error(
              "Failed to release stock for product: {}", item.getProductId(), rollbackException);
        }
      }

      throw exception;
    }

    Order order = new Order();

    order.setOrderNumber("ORD-" + UUID.randomUUID());

    // Customer ID comes from the authenticated JWT through the Gateway
    order.setCustomerId(customerId);

    order.setItems(orderItems);
    order.setTotalAmount(totalAmount);
    order.setStatus(OrderStatus.CREATED);
    order.setPaymentStatus(PaymentStatus.PENDING);

    Order savedOrder = orderRepository.save(order);

    return mapToResponse(savedOrder);
  }

  @Override
  public OrderResponse getOrderById(String id) {

    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

    return mapToResponse(order);
  }

  @Override
  public OrderResponse getOrderByNumber(String orderNumber) {

    Order order =
        orderRepository
            .findByOrderNumber(orderNumber)
            .orElseThrow(
                () -> new OrderNotFoundException("Order not found with number: " + orderNumber));

    return mapToResponse(order);
  }

  private OrderResponse mapToResponse(Order order) {

    List<OrderItemResponse> items =
        order.getItems() == null
            ? List.of()
            : order.getItems().stream()
                .map(
                    item ->
                        new OrderItemResponse(
                            item.getProductId(),
                            item.getProductName(),
                            item.getPrice(),
                            item.getQuantity(),
                            item.getSubtotal()))
                .toList();

    return new OrderResponse(
        order.getId(),
        order.getOrderNumber(),
        order.getCustomerId(),
        items,
        order.getTotalAmount(),
        order.getStatus(),
        order.getPaymentStatus(),
        order.getCreatedAt());
  }

  @Override
  public OrderResponse cancelOrder(String orderId) {

    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

    OrderStatus currentStatus = order.getStatus();

    if (currentStatus == OrderStatus.CANCELLED) {
      throw new IllegalStateException("Order is already cancelled");
    }

    if (currentStatus == OrderStatus.SHIPPED) {
      throw new IllegalStateException("Shipped order cannot be cancelled");
    }

    if (currentStatus == OrderStatus.DELIVERED) {
      throw new IllegalStateException("Delivered order cannot be cancelled");
    }

    if (order.getItems() != null) {
      for (OrderItem item : order.getItems()) {
        productServiceClient.releaseStock(item.getProductId(), item.getQuantity());
      }
    }

    if (order.getPaymentStatus() == PaymentStatus.PAID) {
      order.setPaymentStatus(PaymentStatus.REFUNDED);
    }

    order.setStatus(OrderStatus.CANCELLED);

    Order cancelledOrder = orderRepository.save(order);

    return mapToResponse(cancelledOrder);
  }

  @Override
  public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {

    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

    validateStatusTransition(order, newStatus);

    order.setStatus(newStatus);

    Order updatedOrder = orderRepository.save(order);

    return mapToResponse(updatedOrder);
  }

  private void validateStatusTransition(Order order, OrderStatus next) {

    OrderStatus current = order.getStatus();

    if (current == OrderStatus.CANCELLED) {
      throw new IllegalStateException("Cancelled order cannot be updated");
    }

    if (current == OrderStatus.DELIVERED) {
      throw new IllegalStateException("Delivered order cannot be updated");
    }

    if (current == OrderStatus.CREATED) {

      if (next == OrderStatus.CONFIRMED) {

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
          throw new IllegalStateException("Order cannot be confirmed until payment is completed");
        }

        return;
      }

      if (next == OrderStatus.CANCELLED) {
        return;
      }

      throw new IllegalStateException("CREATED order can only be CONFIRMED or CANCELLED");
    }

    if (current == OrderStatus.CONFIRMED) {

      if (next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED) {
        return;
      }

      throw new IllegalStateException("CONFIRMED order can only be PROCESSING or CANCELLED");
    }

    if (current == OrderStatus.PROCESSING) {

      if (next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED) {
        return;
      }

      throw new IllegalStateException("PROCESSING order can only be SHIPPED or CANCELLED");
    }

    if (current == OrderStatus.SHIPPED) {

      if (next == OrderStatus.DELIVERED) {
        return;
      }

      throw new IllegalStateException("SHIPPED order can only be DELIVERED");
    }
  }

  @Override
  public OrderResponse updatePaymentStatus(String orderId, PaymentStatus paymentStatus) {

    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

    OrderStatus orderStatus = order.getStatus();
    PaymentStatus currentPaymentStatus = order.getPaymentStatus();

    if (orderStatus == OrderStatus.CANCELLED) {
      throw new IllegalStateException("Payment cannot be updated for a cancelled order");
    }

    if (orderStatus == OrderStatus.DELIVERED) {
      throw new IllegalStateException("Payment cannot be updated for a delivered order");
    }

    if (currentPaymentStatus == PaymentStatus.PAID) {
      throw new IllegalStateException("Paid order payment status cannot be changed");
    }

    if (currentPaymentStatus == PaymentStatus.PENDING) {

      if (paymentStatus != PaymentStatus.PAID && paymentStatus != PaymentStatus.FAILED) {

        throw new IllegalStateException("PENDING payment can only be changed to PAID or FAILED");
      }
    }

    if (currentPaymentStatus == PaymentStatus.FAILED) {

      if (paymentStatus != PaymentStatus.PAID) {
        throw new IllegalStateException("FAILED payment can only be changed to PAID");
      }
    }

    order.setPaymentStatus(paymentStatus);

    Order updatedOrder = orderRepository.save(order);

    return mapToResponse(updatedOrder);
  }

  @Override
  public List<OrderResponse> getMyOrders(String customerId) {

    log.info("GET MY ORDERS - CUSTOMER ID: {}", customerId);

    List<Order> orders = orderRepository.findByCustomerId(customerId);

    log.info("GET MY ORDERS - FOUND {} ORDERS", orders.size());

    return orders.stream().map(this::mapToResponse).toList();
  }

  @Override
  public List<OrderResponse> getAllOrders() {

    log.info("GET ALL ORDERS CALLED");

    List<Order> orders = orderRepository.findAll();

    log.info("GET ALL ORDERS - FOUND {} ORDERS", orders.size());

    List<OrderResponse> responses = new ArrayList<>();

    for (Order order : orders) {

      try {

        log.info("MAPPING ORDER: {}", order.getId());

        OrderResponse response = mapToResponse(order);

        responses.add(response);

      } catch (Exception exception) {

        log.error("FAILED TO MAP ORDER: {}", order.getId(), exception);

        throw exception;
      }
    }

    return responses;
  }
}
