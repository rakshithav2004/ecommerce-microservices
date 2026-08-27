package com.ecommerce.order.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ecommerce.order.model.Order;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderRepositoryTest {

  @Autowired private OrderRepository orderRepository;

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();

    Order order1 = new Order();
    order1.setOrderNumber("ORD-001");
    order1.setCustomerId("CUSTOMER-001");

    Order order2 = new Order();
    order2.setOrderNumber("ORD-002");
    order2.setCustomerId("CUSTOMER-002");

    Order order3 = new Order();
    order3.setOrderNumber("ORD-003");
    order3.setCustomerId("CUSTOMER-001");

    orderRepository.save(order1);
    orderRepository.save(order2);
    orderRepository.save(order3);
  }

  @Test
  void shouldFindOrderByOrderNumber() {

    Optional<Order> result = orderRepository.findByOrderNumber("ORD-001");

    assertTrue(result.isPresent());
    assertEquals("ORD-001", result.get().getOrderNumber());
    assertEquals("CUSTOMER-001", result.get().getCustomerId());
  }

  @Test
  void shouldReturnEmptyWhenOrderNumberDoesNotExist() {

    Optional<Order> result = orderRepository.findByOrderNumber("INVALID-ORDER");

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldCheckIfOrderNumberExists() {

    boolean result = orderRepository.existsByOrderNumber("ORD-001");

    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenOrderNumberDoesNotExist() {

    boolean result = orderRepository.existsByOrderNumber("INVALID-ORDER");

    assertFalse(result);
  }

  @Test
  void shouldCheckIfCustomerHasOrders() {

    boolean result = orderRepository.existsByCustomerId("CUSTOMER-001");

    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenCustomerHasNoOrders() {

    boolean result = orderRepository.existsByCustomerId("CUSTOMER-999");

    assertFalse(result);
  }
}
