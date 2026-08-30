package com.ecommerce.product.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {

    Map<String, Object> error =
        Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.NOT_FOUND.value(),
            "error", "Not Found",
            "message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(ProductAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> handleProductAlreadyExists(
      ProductAlreadyExistsException ex) {

    Map<String, Object> error =
        Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.CONFLICT.value(),
            "error", "Conflict",
            "message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(
      MethodArgumentNotValidException ex) {

    Map<String, String> validationErrors = new HashMap<>();

    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

    Map<String, Object> response =
        Map.of(
            "timestamp",
            LocalDateTime.now(),
            "status",
            HttpStatus.BAD_REQUEST.value(),
            "error",
            "Bad Request",
            "message",
            "Validation failed",
            "errors",
            validationErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {

    Map<String, Object> error =
        Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Bad Request",
            "message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "timestamp",
                LocalDateTime.now(),
                "status",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error",
                "INTERNAL_SERVER_ERROR",
                "message",
                "An unexpected error occurred"));
  }
}
