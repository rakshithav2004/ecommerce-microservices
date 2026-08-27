package com.ecommerce.product.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldHandleProductNotFoundException() {

    ProductNotFoundException exception =
        new ProductNotFoundException("Product not found: product-123");

    ResponseEntity<Map<String, Object>> response = handler.handleProductNotFound(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals(404, response.getBody().get("status"));

    assertEquals("Not Found", response.getBody().get("error"));

    assertEquals("Product not found: product-123", response.getBody().get("message"));

    assertNotNull(response.getBody().get("timestamp"));
  }

  @Test
  void shouldHandleProductAlreadyExistsException() {

    ProductAlreadyExistsException exception =
        new ProductAlreadyExistsException("Product with SKU already exists: PHONE-001");

    ResponseEntity<Map<String, Object>> response = handler.handleProductAlreadyExists(exception);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals(409, response.getBody().get("status"));

    assertEquals("Conflict", response.getBody().get("error"));

    assertEquals("Product with SKU already exists: PHONE-001", response.getBody().get("message"));

    assertNotNull(response.getBody().get("timestamp"));
  }

  @Test
  void shouldHandleValidationErrors() {

    BindingResult bindingResult = mock(BindingResult.class);

    FieldError fieldError = new FieldError("productRequest", "name", "Product name is required");

    when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

    when(exception.getBindingResult()).thenReturn(bindingResult);

    ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals(400, response.getBody().get("status"));

    assertEquals("Bad Request", response.getBody().get("error"));

    assertEquals("Validation failed", response.getBody().get("message"));

    assertNotNull(response.getBody().get("timestamp"));

    @SuppressWarnings("unchecked")
    Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");

    assertEquals("Product name is required", errors.get("name"));
  }
}
