package com.ecommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ecommerce.order.dto.OrderItemRequest;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock private OrderRepository orderRepository;

  @Mock private ProductServiceClient productServiceClient;

  @InjectMocks private OrderServiceImpl orderService;

  private ProductResponse product;
  private Order order;

  @BeforeEach
  void setUp() {

    product =
        new ProductResponse(
            "product-1",
            "PHONE-001",
            "iPhone",
            "Electronics",
            "Smartphone",
            new BigDecimal("50000"),
            10,
            true);

    order = new Order();
    order.setId("order-1");
    order.setOrderNumber("ORD-123");
    order.setCustomerId("customer-1");
    order.setStatus(OrderStatus.CREATED);
    order.setPaymentStatus(PaymentStatus.PENDING);
  }

  @Test
  void createOrderShouldCreateOrderSuccessfully() {

    OrderItemRequest itemRequest = new OrderItemRequest("product-1", 2);

    OrderRequest request = new OrderRequest(List.of(itemRequest));

    when(productServiceClient.getProductById("product-1")).thenReturn(product);

    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            invocation -> {
              Order saved = invocation.getArgument(0);
              saved.setId("order-1");
              saved.setOrderNumber("ORD-123");

              return saved;
            });

    OrderResponse response = orderService.createOrder(request, "customer-1");

    assertThat(response).isNotNull();
    assertThat(response.customerId()).isEqualTo("customer-1");
    assertThat(response.totalAmount()).isEqualByComparingTo("100000");
    assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
    assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PENDING);

    verify(productServiceClient).getProductById("product-1");

    verify(productServiceClient).reserveStock("product-1", 2);

    verify(orderRepository).save(any(Order.class));
  }

  @Test
  void createOrderShouldRejectDuplicateProducts() {

    OrderItemRequest item1 = new OrderItemRequest("product-1", 1);

    OrderItemRequest item2 = new OrderItemRequest("product-1", 2);

    OrderRequest request = new OrderRequest(List.of(item1, item2));

    assertThatThrownBy(() -> orderService.createOrder(request, "customer-1"))
        .isInstanceOf(DuplicateOrderItemException.class)
        .hasMessageContaining("Product cannot appear more than once");

    verifyNoInteractions(productServiceClient);
    verifyNoInteractions(orderRepository);
  }

  @Test
  void getOrderByIdShouldReturnOrder() {

    OrderItem item =
        new OrderItem("product-1", "iPhone", new BigDecimal("50000"), 2, new BigDecimal("100000"));

    order.setItems(List.of(item));
    order.setTotalAmount(new BigDecimal("100000"));

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    OrderResponse response = orderService.getOrderById("order-1");

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo("order-1");
    assertThat(response.orderNumber()).isEqualTo("ORD-123");
    assertThat(response.customerId()).isEqualTo("customer-1");
    assertThat(response.items()).hasSize(1);

    verify(orderRepository).findById("order-1");
  }

  @Test
  void getOrderByIdShouldThrowWhenOrderDoesNotExist() {

    when(orderRepository.findById("invalid-id")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.getOrderById("invalid-id"))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("Order not found");

    verify(orderRepository).findById("invalid-id");
  }

  @Test
  void getOrderByNumberShouldReturnOrder() {

    when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(order));

    OrderResponse response = orderService.getOrderByNumber("ORD-123");

    assertThat(response).isNotNull();
    assertThat(response.orderNumber()).isEqualTo("ORD-123");
    assertThat(response.customerId()).isEqualTo("customer-1");

    verify(orderRepository).findByOrderNumber("ORD-123");
  }

  @Test
  void cancelCreatedOrderShouldReleaseStock() {

    OrderItem item =
        new OrderItem("product-1", "iPhone", new BigDecimal("50000"), 2, new BigDecimal("100000"));

    order.setItems(List.of(item));

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.cancelOrder("order-1");

    assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);

    verify(productServiceClient).releaseStock("product-1", 2);

    verify(orderRepository).save(order);
  }

  @Test
  void cancelPaidOrderShouldRefundPayment() {

    order.setPaymentStatus(PaymentStatus.PAID);
    order.setItems(List.of());

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.cancelOrder("order-1");

    assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);

    assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
  }

  @Test
  void cancelAlreadyCancelledOrderShouldThrow() {

    order.setStatus(OrderStatus.CANCELLED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.cancelOrder("order-1"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Order is already cancelled");

    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void cancelShippedOrderShouldThrow() {

    order.setStatus(OrderStatus.SHIPPED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.cancelOrder("order-1"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Shipped order cannot be cancelled");
  }

  @Test
  void cancelDeliveredOrderShouldThrow() {

    order.setStatus(OrderStatus.DELIVERED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.cancelOrder("order-1"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Delivered order cannot be cancelled");
  }

  @Test
  void createdOrderShouldMoveToConfirmedWhenPaymentIsPaid() {

    order.setPaymentStatus(PaymentStatus.PAID);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.updateOrderStatus("order-1", OrderStatus.CONFIRMED);

    assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);

    verify(orderRepository).save(order);
  }

  @Test
  void createdOrderShouldNotBeConfirmedWhenPaymentIsPending() {

    order.setPaymentStatus(PaymentStatus.PENDING);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updateOrderStatus("order-1", OrderStatus.CONFIRMED))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Order cannot be confirmed until payment is completed");

    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void confirmedOrderShouldMoveToProcessing() {

    order.setStatus(OrderStatus.CONFIRMED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.updateOrderStatus("order-1", OrderStatus.PROCESSING);

    assertThat(response.status()).isEqualTo(OrderStatus.PROCESSING);
  }

  @Test
  void processingOrderShouldMoveToShipped() {

    order.setStatus(OrderStatus.PROCESSING);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.updateOrderStatus("order-1", OrderStatus.SHIPPED);

    assertThat(response.status()).isEqualTo(OrderStatus.SHIPPED);
  }

  @Test
  void shippedOrderShouldMoveToDelivered() {

    order.setStatus(OrderStatus.SHIPPED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.updateOrderStatus("order-1", OrderStatus.DELIVERED);

    assertThat(response.status()).isEqualTo(OrderStatus.DELIVERED);
  }

  @Test
  void deliveredOrderShouldNotBeUpdated() {

    order.setStatus(OrderStatus.DELIVERED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updateOrderStatus("order-1", OrderStatus.CANCELLED))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Delivered order cannot be updated");

    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void cancelledOrderShouldNotBeUpdated() {

    order.setStatus(OrderStatus.CANCELLED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updateOrderStatus("order-1", OrderStatus.PROCESSING))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cancelled order cannot be updated");
  }

  @Test
  void confirmedOrderShouldNotMoveDirectlyToShipped() {

    order.setStatus(OrderStatus.CONFIRMED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updateOrderStatus("order-1", OrderStatus.SHIPPED))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("CONFIRMED order can only be PROCESSING or CANCELLED");
  }

  @Test
  void pendingPaymentShouldBecomePaid() {

    order.setPaymentStatus(PaymentStatus.PENDING);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.updatePaymentStatus("order-1", PaymentStatus.PAID);

    assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
  }

  @Test
  void pendingPaymentShouldBecomeFailed() {

    order.setPaymentStatus(PaymentStatus.PENDING);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.updatePaymentStatus("order-1", PaymentStatus.FAILED);

    assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.FAILED);
  }

  @Test
  void failedPaymentShouldBecomePaid() {

    order.setPaymentStatus(PaymentStatus.FAILED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.updatePaymentStatus("order-1", PaymentStatus.PAID);

    assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
  }

  @Test
  void paidPaymentShouldNotBeChanged() {

    order.setPaymentStatus(PaymentStatus.PAID);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updatePaymentStatus("order-1", PaymentStatus.FAILED))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Paid order payment status cannot be changed");

    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void cancelledOrderPaymentShouldNotBeUpdated() {

    order.setStatus(OrderStatus.CANCELLED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updatePaymentStatus("order-1", PaymentStatus.PAID))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Payment cannot be updated for a cancelled order");
  }

  @Test
  void deliveredOrderPaymentShouldNotBeUpdated() {

    order.setStatus(OrderStatus.DELIVERED);

    when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updatePaymentStatus("order-1", PaymentStatus.PAID))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Payment cannot be updated for a delivered order");
  }

  @Test
  void getMyOrdersShouldReturnCustomerOrders() {

    order.setItems(List.of());
    order.setTotalAmount(BigDecimal.ZERO);

    Order order2 = new Order();
    order2.setId("order-2");
    order2.setOrderNumber("ORD-456");
    order2.setCustomerId("customer-1");
    order2.setStatus(OrderStatus.CREATED);
    order2.setPaymentStatus(PaymentStatus.PENDING);
    order2.setItems(List.of());
    order2.setTotalAmount(BigDecimal.ZERO);

    when(orderRepository.findByCustomerId("customer-1")).thenReturn(List.of(order, order2));

    List<OrderResponse> responses = orderService.getMyOrders("customer-1");

    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).customerId()).isEqualTo("customer-1");
    assertThat(responses.get(1).customerId()).isEqualTo("customer-1");

    verify(orderRepository).findByCustomerId("customer-1");
  }

  @Test
  void getAllOrdersShouldReturnAllOrders() {

    order.setItems(List.of());
    order.setTotalAmount(BigDecimal.ZERO);

    when(orderRepository.findAll()).thenReturn(List.of(order));

    List<OrderResponse> responses = orderService.getAllOrders();

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).id()).isEqualTo("order-1");

    verify(orderRepository).findAll();
  }
}
