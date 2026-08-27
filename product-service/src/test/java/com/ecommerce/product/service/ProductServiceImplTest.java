package com.ecommerce.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.ProductAlreadyExistsException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  @Mock private ProductRepository productRepository;

  @Mock private MongoTemplate mongoTemplate;

  @InjectMocks private ProductServiceImpl productService;

  private ProductRequest productRequest;
  private Product product;

  @BeforeEach
  void setUp() {

    productRequest =
        new ProductRequest(
            "PHONE-001",
            "Samsung Galaxy",
            "Electronics",
            "Samsung smartphone",
            new BigDecimal("25000"),
            10);

    product =
        Product.builder()
            .id("product-001")
            .sku("PHONE-001")
            .name("Samsung Galaxy")
            .category("Electronics")
            .description("Samsung smartphone")
            .price(new BigDecimal("25000"))
            .stock(10)
            .active(true)
            .build();
  }

  @Test
  void createProduct_shouldCreateSuccessfully() {

    when(productRepository.existsBySku("PHONE-001")).thenReturn(false);

    when(productRepository.save(any(Product.class))).thenReturn(product);

    ProductResponse response = productService.createProduct(productRequest);

    assertNotNull(response);
    assertEquals("product-001", response.id());
    assertEquals("PHONE-001", response.sku());
    assertEquals("Samsung Galaxy", response.name());
    assertEquals(new BigDecimal("25000"), response.price());
    assertEquals(10, response.stock());
    assertTrue(response.active());

    verify(productRepository).existsBySku("PHONE-001");
    verify(productRepository).save(any(Product.class));
  }

  @Test
  void createProduct_shouldThrowExceptionWhenSkuAlreadyExists() {

    when(productRepository.existsBySku("PHONE-001")).thenReturn(true);

    assertThrows(
        ProductAlreadyExistsException.class, () -> productService.createProduct(productRequest));

    verify(productRepository).existsBySku("PHONE-001");
    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  void getProductById_shouldReturnProduct() {

    when(productRepository.findById("product-001")).thenReturn(Optional.of(product));

    ProductResponse response = productService.getProductById("product-001");

    assertNotNull(response);
    assertEquals("product-001", response.id());
    assertEquals("PHONE-001", response.sku());
    assertEquals("Samsung Galaxy", response.name());

    verify(productRepository).findById("product-001");
  }

  @Test
  void getProductById_shouldThrowExceptionWhenProductNotFound() {

    when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> productService.getProductById("invalid-id"));

    verify(productRepository).findById("invalid-id");
  }

  @Test
  void updateProduct_shouldUpdateSuccessfully() {

    when(productRepository.findById("product-001")).thenReturn(Optional.of(product));

    when(productRepository.save(any(Product.class))).thenReturn(product);

    ProductResponse response = productService.updateProduct("product-001", productRequest);

    assertNotNull(response);
    assertEquals("PHONE-001", response.sku());
    assertEquals("Samsung Galaxy", response.name());

    verify(productRepository).findById("product-001");
    verify(productRepository).save(product);
  }

  @Test
  void updateProduct_shouldThrowExceptionWhenProductNotFound() {

    when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

    assertThrows(
        ProductNotFoundException.class,
        () -> productService.updateProduct("invalid-id", productRequest));

    verify(productRepository).findById("invalid-id");
    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  void updateProduct_shouldThrowExceptionWhenNewSkuAlreadyExists() {

    ProductRequest updatedRequest =
        new ProductRequest(
            "PHONE-002",
            "Samsung Galaxy",
            "Electronics",
            "Samsung smartphone",
            new BigDecimal("25000"),
            10);

    when(productRepository.findById("product-001")).thenReturn(Optional.of(product));

    when(productRepository.existsBySku("PHONE-002")).thenReturn(true);

    assertThrows(
        ProductAlreadyExistsException.class,
        () -> productService.updateProduct("product-001", updatedRequest));

    verify(productRepository).existsBySku("PHONE-002");

    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  void deleteProduct_shouldDeleteSuccessfully() {

    when(productRepository.findById("product-001")).thenReturn(Optional.of(product));

    productService.deleteProduct("product-001");

    verify(productRepository).findById("product-001");
    verify(productRepository).delete(product);
  }

  @Test
  void deleteProduct_shouldThrowExceptionWhenProductNotFound() {

    when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct("invalid-id"));

    verify(productRepository, never()).delete(any(Product.class));
  }

  @Test
  void reserveStock_shouldReduceStockSuccessfully() {

    Product updatedProduct =
        Product.builder()
            .id("product-001")
            .sku("PHONE-001")
            .name("Samsung Galaxy")
            .category("Electronics")
            .description("Samsung smartphone")
            .price(new BigDecimal("25000"))
            .stock(7)
            .active(true)
            .build();

    when(mongoTemplate.findAndModify(
            any(), any(), any(FindAndModifyOptions.class), eq(Product.class)))
        .thenReturn(updatedProduct);

    ProductResponse response = productService.reserveStock("product-001", 3);

    assertNotNull(response);
    assertEquals("product-001", response.id());
    assertEquals("PHONE-001", response.sku());
    assertEquals("Samsung Galaxy", response.name());
    assertEquals(7, response.stock());

    verify(mongoTemplate)
        .findAndModify(any(), any(), any(FindAndModifyOptions.class), eq(Product.class));
    verify(productRepository, never()).findById(anyString());
  }

  @Test
  void reserveStock_shouldThrowExceptionForInvalidQuantity() {

    assertThrows(
        IllegalArgumentException.class, () -> productService.reserveStock("product-001", 0));
    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  void reserveStock_shouldThrowExceptionForInsufficientStock() {

    when(productRepository.findById("product-001")).thenReturn(Optional.of(product));

    assertThrows(
        IllegalArgumentException.class, () -> productService.reserveStock("product-001", 20));

    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  void reserveStock_shouldThrowExceptionWhenProductNotFound() {

    when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

    assertThrows(
        ProductNotFoundException.class, () -> productService.reserveStock("invalid-id", 2));
  }

  @Test
  void releaseStock_shouldIncreaseStockSuccessfully() {

    when(productRepository.findById("product-001")).thenReturn(Optional.of(product));

    when(productRepository.save(any(Product.class))).thenReturn(product);

    ProductResponse response = productService.releaseStock("product-001", 5);

    assertNotNull(response);
    assertEquals(15, product.getStock());

    verify(productRepository).findById("product-001");
    verify(productRepository).save(product);
  }

  @Test
  void releaseStock_shouldThrowExceptionForInvalidQuantity() {

    when(productRepository.findById("product-001")).thenReturn(Optional.of(product));

    assertThrows(
        IllegalArgumentException.class, () -> productService.releaseStock("product-001", 0));

    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  void releaseStock_shouldThrowExceptionWhenProductNotFound() {

    when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

    assertThrows(
        ProductNotFoundException.class, () -> productService.releaseStock("invalid-id", 5));
  }

  @Test
  void getAllProducts_shouldReturnProducts() {

    Pageable pageable = PageRequest.of(0, 10);

    when(mongoTemplate.count(any(), eq(Product.class))).thenReturn(1L);

    when(mongoTemplate.find(any(), eq(Product.class))).thenReturn(List.of(product));

    Page<ProductResponse> result =
        productService.getAllProducts(null, null, null, null, null, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getContent().size());

    assertEquals("PHONE-001", result.getContent().get(0).sku());

    verify(mongoTemplate).count(any(), eq(Product.class));

    verify(mongoTemplate).find(any(), eq(Product.class));
  }

  @Test
  void getAllProducts_shouldReturnEmptyPageWhenNoProductsFound() {

    Pageable pageable = PageRequest.of(0, 10);

    when(mongoTemplate.count(any(), eq(Product.class))).thenReturn(0L);

    when(mongoTemplate.find(any(), eq(Product.class))).thenReturn(List.of());

    Page<ProductResponse> result =
        productService.getAllProducts(null, null, null, null, null, pageable);

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());
  }
}
