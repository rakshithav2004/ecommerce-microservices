package com.ecommerce.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.PaymentStatus;
import com.ecommerce.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock private OrderRepository orderRepository;

  @Mock private ProductServiceClient productServiceClient;

  private OrderServiceImpl orderService() {
    return new OrderServiceImpl(orderRepository, productServiceClient);
  }

  @Test
  void createOrder_shouldCreateOrderSuccessfully() {

    OrderItemRequest itemRequest = new OrderItemRequest("product-001", 2);

    OrderRequest request = new OrderRequest("CUST-001", List.of(itemRequest));

    ProductResponse product =
        new ProductResponse(
            "product-001",
            "PHONE-001",
            "Samsung Galaxy",
            "Electronics",
            "Smartphone",
            new BigDecimal("25000"),
            10,
            true);

    when(productServiceClient.getProductById("product-001")).thenReturn(product);

    when(productServiceClient.reserveStock("product-001", 2)).thenReturn(product);

    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.setId("order-001");
              return order;
            });

    OrderResponse response = orderService().createOrder(request);

    assertNotNull(response);
    assertEquals("order-001", response.id());
    assertEquals("CUST-001", response.customerId());
    assertEquals(new BigDecimal("50000"), response.totalAmount());
    assertEquals(OrderStatus.CREATED, response.status());
    assertEquals(PaymentStatus.PENDING, response.paymentStatus());

    verify(productServiceClient).getProductById("product-001");

    verify(productServiceClient).reserveStock("product-001", 2);

    verify(orderRepository).save(any(Order.class));
  }

  @Test
  void getOrderById_shouldReturnOrder() {

    Order order = createOrder();

    when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

    OrderResponse response = orderService().getOrderById("order-001");

    assertNotNull(response);
    assertEquals("order-001", response.id());
    assertEquals("ORD-001", response.orderNumber());
    assertEquals("CUST-001", response.customerId());

    verify(orderRepository).findById("order-001");
  }

  @Test
  void getOrderById_shouldThrowExceptionWhenNotFound() {

    when(orderRepository.findById("invalid-id")).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> orderService().getOrderById("invalid-id"));

    verify(orderRepository).findById("invalid-id");
  }

  @Test
  void getOrderByNumber_shouldReturnOrder() {

    Order order = createOrder();

    when(orderRepository.findByOrderNumber("ORD-001")).thenReturn(Optional.of(order));

    OrderResponse response = orderService().getOrderByNumber("ORD-001");

    assertNotNull(response);
    assertEquals("ORD-001", response.orderNumber());

    verify(orderRepository).findByOrderNumber("ORD-001");
  }

  @Test
  void getOrderByNumber_shouldThrowExceptionWhenNotFound() {

    when(orderRepository.findByOrderNumber("INVALID")).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> orderService().getOrderByNumber("INVALID"));

    verify(orderRepository).findByOrderNumber("INVALID");
  }

  @Test
  void cancelOrder_shouldCancelOrderAndReleaseStock() {

    Order order = createOrder();

    when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

    when(productServiceClient.releaseStock("product-001", 2)).thenReturn(null);

    when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    OrderResponse response = orderService().cancelOrder("order-001");

    assertNotNull(response);
    assertEquals(OrderStatus.CANCELLED, response.status());

    verify(productServiceClient).releaseStock("product-001", 2);

    verify(orderRepository).save(order);
  }

  @Test
  void cancelOrder_shouldThrowExceptionWhenAlreadyCancelled() {

    Order order = createOrder();
    order.setStatus(OrderStatus.CANCELLED);

    when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

    assertThrows(IllegalStateException.class, () -> orderService().cancelOrder("order-001"));

    verify(productServiceClient, never()).releaseStock(anyString(), anyInt());

    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void updateOrderStatus_shouldUpdateSuccessfully() {

    Order order = new Order();

    order.setId("ORD-001");
    order.setOrderNumber("ORD-001");
    order.setCustomerId("CUST-001");

    OrderItem item =
        new OrderItem(
            "product-001", "Samsung Galaxy", new BigDecimal("25000"), 2, new BigDecimal("50000"));

    order.setItems(List.of(item));
    order.setTotalAmount(new BigDecimal("50000"));

    order.setStatus(OrderStatus.CREATED);

    // Use the successful payment status from your enum
    order.setPaymentStatus(PaymentStatus.PAID);

    when(orderRepository.findById("ORD-001")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService().updateOrderStatus("ORD-001", OrderStatus.CONFIRMED);

    assertNotNull(response);

    assertEquals(OrderStatus.CONFIRMED, response.status());

    assertEquals(OrderStatus.CONFIRMED, order.getStatus());

    verify(orderRepository).findById("ORD-001");

    verify(orderRepository).save(order);
  }

  @Test
  void updateOrderStatus_shouldRejectInvalidTransition() {

    Order order = createOrder();

    order.setStatus(OrderStatus.CREATED);

    when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

    assertThrows(
        IllegalStateException.class,
        () -> orderService().updateOrderStatus("order-001", OrderStatus.SHIPPED));

    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void updateOrderStatus_shouldRejectCancelledOrder() {

    Order order = createOrder();
    order.setStatus(OrderStatus.CANCELLED);

    when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

    assertThrows(
        IllegalStateException.class,
        () -> orderService().updateOrderStatus("order-001", OrderStatus.CONFIRMED));

    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void updateOrderStatus_shouldRejectDeliveredOrder() {

    Order order = createOrder();
    order.setStatus(OrderStatus.DELIVERED);

    when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

    assertThrows(
        IllegalStateException.class,
        () -> orderService().updateOrderStatus("order-001", OrderStatus.CONFIRMED));

    verify(orderRepository, never()).save(any(Order.class));
  }

  private Order createOrder() {

    OrderItem item =
        new OrderItem(
            "product-001", "Samsung Galaxy", new BigDecimal("25000"), 2, new BigDecimal("50000"));

    Order order = new Order();

    order.setId("order-001");
    order.setOrderNumber("ORD-001");
    order.setCustomerId("CUST-001");
    order.setItems(List.of(item));
    order.setTotalAmount(new BigDecimal("50000"));
    order.setStatus(OrderStatus.CREATED);
    order.setPaymentStatus(PaymentStatus.PENDING);

    return order;
  }
}
