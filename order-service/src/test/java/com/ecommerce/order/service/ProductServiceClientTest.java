package com.ecommerce.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.ProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceClientTest {

  @Mock private ProductClient productClient;

  @InjectMocks private ProductServiceClient productServiceClient;

  @Test
  void shouldGetProductById() {
    ProductResponse response =
        new ProductResponse(
            "product-123",
            "PHONE-001",
            "iPhone 15",
            "Electronics",
            "Apple smartphone",
            new BigDecimal("69999"),
            10,
            true);

    when(productClient.getProductById("product-123")).thenReturn(response);

    ProductResponse result = productServiceClient.getProductById("product-123");

    assertNotNull(result);

    assertEquals("product-123", result.id());
    assertEquals("PHONE-001", result.sku());
    assertEquals("iPhone 15", result.name());
    assertEquals("Electronics", result.category());
    assertEquals("Apple smartphone", result.description());
    assertEquals(new BigDecimal("69999"), result.price());
    assertEquals(10, result.stock());
    assertTrue(result.active());

    verify(productClient, times(1)).getProductById("product-123");
  }

  @Test
  void shouldThrowExceptionWhenGetProductByIdFallbackIsCalled() {

    Throwable throwable = new RuntimeException("Product Service unavailable");

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> productServiceClient.getProductByIdFallback("product-123", throwable));

    assertEquals(
        "Product Service is currently unavailable. " + "Please try again later.",
        exception.getMessage());
  }

  @Test
  void shouldReserveStock() {

    ProductResponse response =
        new ProductResponse(
            "product-123",
            "PHONE-001",
            "iPhone 15",
            "Electronics",
            "Apple smartphone",
            new BigDecimal("69999"),
            10,
            true);

    when(productClient.reserveStock("product-123", 5)).thenReturn(response);

    ProductResponse result = productServiceClient.reserveStock("product-123", 5);

    assertNotNull(result);

    assertEquals("product-123", result.id());
    assertEquals("PHONE-001", result.sku());
    assertEquals("iPhone 15", result.name());
    assertEquals(10, result.stock());

    verify(productClient, times(1)).reserveStock("product-123", 5);
  }

  @Test
  void shouldThrowExceptionWhenReserveStockFallbackIsCalled() {

    Throwable throwable = new RuntimeException("Product Service unavailable");

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> productServiceClient.reserveStockFallback("product-123", 5, throwable));

    assertEquals(
        "Product Service is currently unavailable. " + "Unable to reserve stock.",
        exception.getMessage());
  }

  @Test
  void shouldReleaseStock() {

    ProductResponse response =
        new ProductResponse(
            "product-123",
            "PHONE-001",
            "iPhone 15",
            "Electronics",
            "Apple smartphone",
            new BigDecimal("69999"),
            10,
            true);

    when(productClient.releaseStock("product-123", 5)).thenReturn(response);

    ProductResponse result = productServiceClient.releaseStock("product-123", 5);

    assertNotNull(result);

    assertEquals("product-123", result.id());
    assertEquals("PHONE-001", result.sku());
    assertEquals("iPhone 15", result.name());
    assertEquals(10, result.stock());

    verify(productClient, times(1)).releaseStock("product-123", 5);
  }

  @Test
  void shouldThrowExceptionWhenReleaseStockFallbackIsCalled() {

    Throwable throwable = new RuntimeException("Product Service unavailable");

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> productServiceClient.releaseStockFallback("product-123", 5, throwable));

    assertEquals(
        "Product Service is currently unavailable. " + "Unable to release stock.",
        exception.getMessage());
  }
}
