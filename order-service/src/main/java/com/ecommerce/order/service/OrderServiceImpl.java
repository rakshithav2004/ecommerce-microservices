package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.PaymentStatus;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    public OrderResponse createOrder(OrderRequest request) {

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            ProductResponse product =
                    productServiceClient.getProductById(
                            itemRequest.productId()
                    );
            productServiceClient.reserveStock(
                    itemRequest.productId(),
                    itemRequest.quantity()
            );
            BigDecimal subtotal = product.price()
                    .multiply(
                            BigDecimal.valueOf(
                                    itemRequest.quantity()
                            )
                    );
            OrderItem orderItem = new OrderItem(
                    product.id(),
                    product.name(),
                    product.price(),
                    itemRequest.quantity(),
                    subtotal
            );

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        Order order = new Order();
        order.setOrderNumber(
                "ORD-" + UUID.randomUUID()
        );

        order.setCustomerId(
                request.customerId()
        );

        order.setItems(orderItems);

        order.setTotalAmount(
                totalAmount
        );

        order.setStatus(
                OrderStatus.CREATED
        );

        order.setPaymentStatus(
                PaymentStatus.PENDING
        );

        Order savedOrder =
                orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(String id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + id
                        )
                );

        return mapToResponse(order);
    }

    @Override
    public OrderResponse getOrderByNumber(
            String orderNumber) {

        Order order = orderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with number: "
                                        + orderNumber
                        )
                );

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(item ->
                                new OrderItemResponse(
                                        item.getProductId(),
                                        item.getProductName(),
                                        item.getPrice(),
                                        item.getQuantity(),
                                        item.getSubtotal()
                                )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                items,
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getCreatedAt()
        );
    }

    @Override
    public OrderResponse cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Order is already cancelled"
            );
        }

        for (OrderItem item : order.getItems()) {
            productServiceClient.releaseStock(
                    item.getProductId(),
                    item.getQuantity()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder =
                orderRepository.save(order);
        return mapToResponse(cancelledOrder);
    }

    @Override
    public OrderResponse updateOrderStatus(
            String orderId,
            OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        OrderStatus currentStatus = order.getStatus();

        validateStatusTransition(
                currentStatus,
                newStatus
        );

        order.setStatus(newStatus);

        Order updatedOrder =
                orderRepository.save(order);

        return mapToResponse(updatedOrder);
    }

    private void validateStatusTransition(
            OrderStatus current,
            OrderStatus next) {

        if (current == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cancelled order cannot be updated"
            );
        }

        if (current == OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Delivered order cannot be updated"
            );
        }

        if (current == OrderStatus.CREATED
                && next != OrderStatus.CONFIRMED
                && next != OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "CREATED order can only be CONFIRMED or CANCELLED"
            );
        }

        if (current == OrderStatus.CONFIRMED
                && next != OrderStatus.PROCESSING
                && next != OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "CONFIRMED order can only be PROCESSING or CANCELLED"
            );
        }

        if (current == OrderStatus.PROCESSING
                && next != OrderStatus.SHIPPED
                && next != OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "PROCESSING order can only be SHIPPED or CANCELLED"
            );
        }

        if (current == OrderStatus.SHIPPED
                && next != OrderStatus.DELIVERED) {

            throw new IllegalStateException(
                    "SHIPPED order can only be DELIVERED"
            );
        }
    }
}