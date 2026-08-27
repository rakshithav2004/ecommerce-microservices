package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PaymentUpdateRequest;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @Operation(
      summary = "Create a new order",
      description =
          "Places a new order for the given product and quantity, validating stock via product-service")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
    return orderService.createOrder(request);
  }

  @Operation(
      summary = "Get order by ID",
      description = "Retrieves an order using its unique order ID")
  @GetMapping("/{id}")
  public OrderResponse getOrderById(@PathVariable String id) {
    return orderService.getOrderById(id);
  }

  @Operation(
      summary = "Get order by order number",
      description = "Retrieves an order using its unique order number")
  @GetMapping("/number/{orderNumber}")
  public OrderResponse getOrderByNumber(@PathVariable String orderNumber) {
    return orderService.getOrderByNumber(orderNumber);
  }

  @Operation(
      summary = "Cancel an order",
      description = "Cancels an existing order using its unique order ID")
  @PostMapping("/{id}/cancel")
  public OrderResponse cancelOrder(@PathVariable String id) {
    return orderService.cancelOrder(id);
  }

  @Operation(
      summary = "Update order status",
      description = "Updates the status of an existing order")
  @PutMapping("/{id}/status")
  public OrderResponse updateOrderStatus(
      @PathVariable String id, @RequestParam OrderStatus status) {
    return orderService.updateOrderStatus(id, status);
  }

  @PutMapping("/{orderId}/payment")
  public ResponseEntity<OrderResponse> updatePaymentStatus(
      @PathVariable String orderId, @Valid @RequestBody PaymentUpdateRequest request) {
    return ResponseEntity.ok(orderService.updatePaymentStatus(orderId, request.paymentStatus()));
  }
}
