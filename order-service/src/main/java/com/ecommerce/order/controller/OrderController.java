package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(
            @PathVariable String id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/number/{orderNumber}")
    public OrderResponse getOrderByNumber(
            @PathVariable String orderNumber) {
        return orderService.getOrderByNumber(orderNumber);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable String id) {

        return orderService.cancelOrder(id);
    }

    @PutMapping("/{id}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status) {

        return orderService.updateOrderStatus(
                id,
                status
        );
    }
}