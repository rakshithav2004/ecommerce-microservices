package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.ProductAlreadyExistsException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final MongoTemplate mongoTemplate;

  @Override
  public ProductResponse createProduct(ProductRequest request) {
    if (productRepository.existsBySku(request.sku())) {
      throw new ProductAlreadyExistsException("Product with SKU already exists: " + request.sku());
    }

    Product product =
        Product.builder()
            .sku(request.sku())
            .name(request.name())
            .category(request.category())
            .description(request.description())
            .price(request.price())
            .stock(request.stock())
            .active(true)
            .build();

    Product savedProduct = productRepository.save(product);
    return new ProductResponse(
        savedProduct.getId(),
        savedProduct.getSku(),
        savedProduct.getName(),
        savedProduct.getCategory(),
        savedProduct.getDescription(),
        savedProduct.getPrice(),
        savedProduct.getStock(),
        savedProduct.getActive());
  }

  @Override
  public ProductResponse getProductById(String id) {

    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

    return new ProductResponse(
        product.getId(),
        product.getSku(),
        product.getName(),
        product.getCategory(),
        product.getDescription(),
        product.getPrice(),
        product.getStock(),
        product.getActive());
  }

  @Override
  public Page<ProductResponse> getAllProducts(
      String category,
      String search,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Boolean inStock,
      Pageable pageable) {

    Query query = new Query();

    if (category != null && !category.isBlank()) {
      query.addCriteria(Criteria.where("category").regex("^" + category + "$", "i"));
    }

    if (search != null && !search.isBlank()) {
      query.addCriteria(
          new Criteria()
              .orOperator(
                  Criteria.where("name").regex(search, "i"),
                  Criteria.where("description").regex(search, "i")));
    }

    if (minPrice != null || maxPrice != null) {

      Criteria priceCriteria = Criteria.where("price");

      if (minPrice != null) {
        priceCriteria.gte(minPrice);
      }

      if (maxPrice != null) {
        priceCriteria.lte(maxPrice);
      }

      query.addCriteria(priceCriteria);
    }

    if (Boolean.TRUE.equals(inStock)) {
      query.addCriteria(Criteria.where("stock").gt(0));
    }

    long total = mongoTemplate.count(query, Product.class);

    query.with(pageable);

    List<Product> products = mongoTemplate.find(query, Product.class);

    return new PageImpl<>(
        products.stream()
            .map(
                product ->
                    new ProductResponse(
                        product.getId(),
                        product.getSku(),
                        product.getName(),
                        product.getCategory(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStock(),
                        product.getActive()))
            .toList(),
        pageable,
        total);
  }

  @Override
  public ProductResponse updateProduct(String id, ProductRequest request) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

    if (!product.getSku().equals(request.sku()) && productRepository.existsBySku(request.sku())) {
      throw new ProductAlreadyExistsException("Product with SKU already exists: " + request.sku());
    }

    product.setSku(request.sku());
    product.setName(request.name());
    product.setCategory(request.category());
    product.setDescription(request.description());
    product.setPrice(request.price());
    Product updatedProduct = productRepository.save(product);
    return new ProductResponse(
        updatedProduct.getId(),
        updatedProduct.getSku(),
        updatedProduct.getName(),
        updatedProduct.getCategory(),
        updatedProduct.getDescription(),
        updatedProduct.getPrice(),
        updatedProduct.getStock(),
        updatedProduct.getActive());
  }

  @Override
  public void deleteProduct(String id) {

    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

    productRepository.delete(product);
  }

  @Override
  public ProductResponse reserveStock(String productId, int quantity) {

    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    Query query = new Query(Criteria.where("_id").is(productId).and("stock").gte(quantity));

    Update update = new Update().inc("stock", -quantity);

    Product updatedProduct =
        mongoTemplate.findAndModify(
            query, update, FindAndModifyOptions.options().returnNew(true), Product.class);

    if (updatedProduct == null) {

      Product product =
          productRepository
              .findById(productId)
              .orElseThrow(
                  () -> new ProductNotFoundException("Product not found with id: " + productId));

      throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
    }

    return new ProductResponse(
        updatedProduct.getId(),
        updatedProduct.getSku(),
        updatedProduct.getName(),
        updatedProduct.getCategory(),
        updatedProduct.getDescription(),
        updatedProduct.getPrice(),
        updatedProduct.getStock(),
        updatedProduct.getActive());
  }

  @Override
  public ProductResponse releaseStock(String productId, int quantity) {

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ProductNotFoundException("Product not found with id: " + productId));

    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    product.setStock(product.getStock() + quantity);

    Product savedProduct = productRepository.save(product);

    return new ProductResponse(
        savedProduct.getId(),
        savedProduct.getSku(),
        savedProduct.getName(),
        savedProduct.getCategory(),
        savedProduct.getDescription(),
        savedProduct.getPrice(),
        savedProduct.getStock(),
        savedProduct.getActive());
  }
}
