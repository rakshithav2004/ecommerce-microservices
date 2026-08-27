package com.ecommerce.order.exception;

public class ProductServiceException extends RuntimeException {

  private final int status;

  public ProductServiceException(String message, int status) {
    super(message);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
