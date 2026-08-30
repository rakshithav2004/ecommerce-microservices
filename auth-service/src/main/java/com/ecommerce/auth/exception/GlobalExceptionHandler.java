package com.ecommerce.auth.exception;

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

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {

    String message = ex.getMessage() != null ? ex.getMessage() : "Invalid authentication request";

    if (message.toLowerCase().contains("already exists")) {
      return buildResponse(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", message);
    }

    return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(
      MethodArgumentNotValidException ex) {

    Map<String, String> validationErrors = new HashMap<>();

    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

    Map<String, Object> response = new HashMap<>();

    response.put("timestamp", LocalDateTime.now());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "VALIDATION_FAILED");
    response.put("message", "Request validation failed");
    response.put("errors", validationErrors);

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {

    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred");
  }

  private ResponseEntity<Map<String, Object>> buildResponse(
      HttpStatus status, String error, String message) {

    return ResponseEntity.status(status)
        .body(
            Map.of(
                "timestamp",
                LocalDateTime.now(),
                "status",
                status.value(),
                "error",
                error,
                "message",
                message));
  }
}
