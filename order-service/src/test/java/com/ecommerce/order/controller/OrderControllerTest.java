package com.ecommerce.order.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.PaymentStatus;
import com.ecommerce.order.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

  private MockMvc mockMvc;

  @Mock private OrderService orderService;

  @BeforeEach
  void setUp() {
    OrderController orderController = new OrderController(orderService);

    mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
  }

  @Test
  void getOrderById_shouldReturnOrder() throws Exception {

    OrderResponse response =
        new OrderResponse(
            "order-001",
            "ORD-001",
            "CUST-001",
            List.of(),
            new BigDecimal("1000"),
            OrderStatus.CREATED,
            PaymentStatus.PENDING,
            null);

    when(orderService.getOrderById("order-001")).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/orders/order-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("order-001"))
        .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
        .andExpect(jsonPath("$.customerId").value("CUST-001"))
        .andExpect(jsonPath("$.status").value("CREATED"));
  }

  @Test
  void getOrderByNumber_shouldReturnOrder() throws Exception {

    OrderResponse response =
        new OrderResponse(
            "order-001",
            "ORD-001",
            "CUST-001",
            List.of(),
            new BigDecimal("1000"),
            OrderStatus.CREATED,
            PaymentStatus.PENDING,
            null);

    when(orderService.getOrderByNumber("ORD-001")).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/orders/number/ORD-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
        .andExpect(jsonPath("$.customerId").value("CUST-001"));
  }

  @Test
  void cancelOrder_shouldReturnCancelledOrder() throws Exception {

    OrderResponse response =
        new OrderResponse(
            "order-001",
            "ORD-001",
            "CUST-001",
            List.of(),
            new BigDecimal("1000"),
            OrderStatus.CANCELLED,
            PaymentStatus.PENDING,
            null);

    when(orderService.cancelOrder("order-001")).thenReturn(response);

    mockMvc
        .perform(post("/api/v1/orders/order-001/cancel"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"))
        .andExpect(jsonPath("$.orderNumber").value("ORD-001"));
  }

  @Test
  void updateOrderStatus_shouldReturnUpdatedOrder() throws Exception {

    OrderResponse response =
        new OrderResponse(
            "order-001",
            "ORD-001",
            "CUST-001",
            List.of(),
            new BigDecimal("1000"),
            OrderStatus.CONFIRMED,
            PaymentStatus.PENDING,
            null);

    when(orderService.updateOrderStatus("order-001", OrderStatus.CONFIRMED)).thenReturn(response);

    mockMvc
        .perform(put("/api/v1/orders/order-001/status").param("status", "CONFIRMED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"))
        .andExpect(jsonPath("$.orderNumber").value("ORD-001"));
  }

  @Test
  void getMyOrders_shouldReturnCustomerOrders() throws Exception {

    OrderResponse response =
        new OrderResponse(
            "order-001",
            "ORD-001",
            "CUST-001",
            List.of(),
            new BigDecimal("1000"),
            OrderStatus.CREATED,
            PaymentStatus.PENDING,
            null);

    when(orderService.getMyOrders("CUST-001")).thenReturn(List.of(response));

    mockMvc
        .perform(get("/api/v1/orders/my-orders").header("X-Customer-Id", "CUST-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value("order-001"))
        .andExpect(jsonPath("$[0].orderNumber").value("ORD-001"))
        .andExpect(jsonPath("$[0].customerId").value("CUST-001"))
        .andExpect(jsonPath("$[0].status").value("CREATED"));
  }

  @Test
  void getAllOrders_shouldReturnAllOrders() throws Exception {

    OrderResponse response1 =
        new OrderResponse(
            "order-001",
            "ORD-001",
            "CUST-001",
            List.of(),
            new BigDecimal("1000"),
            OrderStatus.CREATED,
            PaymentStatus.PENDING,
            null);

    OrderResponse response2 =
        new OrderResponse(
            "order-002",
            "ORD-002",
            "CUST-002",
            List.of(),
            new BigDecimal("2000"),
            OrderStatus.CONFIRMED,
            PaymentStatus.PENDING,
            null);

    when(orderService.getAllOrders()).thenReturn(List.of(response1, response2));

    mockMvc
        .perform(get("/api/v1/orders/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value("order-001"))
        .andExpect(jsonPath("$[0].orderNumber").value("ORD-001"))
        .andExpect(jsonPath("$[0].customerId").value("CUST-001"))
        .andExpect(jsonPath("$[1].id").value("order-002"))
        .andExpect(jsonPath("$[1].orderNumber").value("ORD-002"))
        .andExpect(jsonPath("$[1].customerId").value("CUST-002"));
  }
}
