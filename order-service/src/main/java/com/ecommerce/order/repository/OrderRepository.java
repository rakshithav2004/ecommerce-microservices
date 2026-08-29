package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
  Optional<Order> findByOrderNumber(String orderNumber);

  boolean existsByOrderNumber(String orderNumber);

  boolean existsByCustomerId(String customerId);

  List<Order> findByCustomerId(String customerId);
}
