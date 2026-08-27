package com.ecommerce.product.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ecommerce.product.model.Product;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
class ProductRepositoryTest {

  @Autowired private ProductRepository productRepository;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();

    Product product1 = new Product();
    product1.setSku("PHONE-001");
    product1.setName("iPhone 15");
    product1.setCategory("Electronics");
    product1.setDescription("Apple smartphone");
    product1.setPrice(new BigDecimal("69999"));
    product1.setStock(10);
    product1.setActive(true);

    Product product2 = new Product();
    product2.setSku("LAPTOP-001");
    product2.setName("Dell Laptop");
    product2.setCategory("Electronics");
    product2.setDescription("Business laptop");
    product2.setPrice(new BigDecimal("65000"));
    product2.setStock(5);
    product2.setActive(true);

    Product product3 = new Product();
    product3.setSku("SHOE-001");
    product3.setName("Running Shoes");
    product3.setCategory("Fashion");
    product3.setDescription("Sports running shoes");
    product3.setPrice(new BigDecimal("2999"));
    product3.setStock(20);
    product3.setActive(true);

    productRepository.save(product1);
    productRepository.save(product2);
    productRepository.save(product3);
  }

  @Test
  void shouldFindProductBySku() {

    Optional<Product> result = productRepository.findBySku("PHONE-001");

    assertTrue(result.isPresent());
    assertEquals("PHONE-001", result.get().getSku());
    assertEquals("iPhone 15", result.get().getName());
  }

  @Test
  void shouldReturnEmptyWhenSkuDoesNotExist() {

    Optional<Product> result = productRepository.findBySku("INVALID-001");

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldCheckIfSkuExists() {

    boolean result = productRepository.existsBySku("PHONE-001");

    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenSkuDoesNotExist() {

    boolean result = productRepository.existsBySku("INVALID-001");

    assertFalse(result);
  }

  @Test
  void shouldFindProductsByCategoryIgnoreCase() {

    Page<Product> result =
        productRepository.findByCategoryIgnoreCase("electronics", PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
  }

  @Test
  void shouldFindProductsByName() {

    Page<Product> result =
        productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            "iphone", "xyz", PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals("PHONE-001", result.getContent().get(0).getSku());
  }

  @Test
  void shouldFindProductsByDescription() {

    Page<Product> result =
        productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            "xyz", "business", PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals("LAPTOP-001", result.getContent().get(0).getSku());
  }

  @Test
  void shouldIgnoreCaseWhenSearching() {

    Page<Product> result =
        productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            "IPHONE", "XYZ", PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals("iPhone 15", result.getContent().get(0).getName());
  }
}
