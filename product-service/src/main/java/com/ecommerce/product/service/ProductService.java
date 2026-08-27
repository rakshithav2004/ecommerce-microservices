package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
  ProductResponse createProduct(ProductRequest request);

  ProductResponse getProductById(String id);

  Page<ProductResponse> getAllProducts(
      String category,
      String search,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Boolean inStock,
      Pageable pageable);

  ProductResponse updateProduct(String id, ProductRequest request);

  void deleteProduct(String id);

  ProductResponse reserveStock(String productId, int quantity);

  ProductResponse releaseStock(String productId, int quantity);
}
