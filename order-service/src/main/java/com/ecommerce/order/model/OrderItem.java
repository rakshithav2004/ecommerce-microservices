package com.ecommerce.order.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

  private String productId;
  private String productName;
  private BigDecimal price;
  private Integer quantity;
  private BigDecimal subtotal;
}
