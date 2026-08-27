package com.ecommerce.product.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

  @Id private String id;

  @Indexed(unique = true)
  private String sku;

  @Indexed private String name;

  @Indexed private String category;

  private String description;
  private BigDecimal price;
  private Integer stock;
  private Boolean active;
}
