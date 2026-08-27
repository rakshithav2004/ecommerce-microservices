package com.ecommerce.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleIllegalStateException_shouldReturn409WhenUserAlreadyExists() {

    IllegalStateException exception =
        new IllegalStateException("User with email already exists: " + "rakshitha@example.com");

    ResponseEntity<Map<String, Object>> response = handler.handleIllegalStateException(exception);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals(409, response.getBody().get("status"));

    assertEquals("USER_ALREADY_EXISTS", response.getBody().get("error"));

    assertEquals(
        "User with email already exists: " + "rakshitha@example.com",
        response.getBody().get("message"));

    assertNotNull(response.getBody().get("timestamp"));
  }

  @Test
  void handleIllegalStateException_shouldReturn401() {

    IllegalStateException exception = new IllegalStateException("Invalid email or password");

    ResponseEntity<Map<String, Object>> response = handler.handleIllegalStateException(exception);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals(401, response.getBody().get("status"));

    assertEquals("UNAUTHORIZED", response.getBody().get("error"));

    assertEquals("Invalid email or password", response.getBody().get("message"));
  }

  @Test
  void handleIllegalStateException_shouldReturnDefaultMessageWhenMessageIsNull() {

    IllegalStateException exception = new IllegalStateException();

    ResponseEntity<Map<String, Object>> response = handler.handleIllegalStateException(exception);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals("UNAUTHORIZED", response.getBody().get("error"));

    assertEquals("Invalid authentication credentials", response.getBody().get("message"));
  }

  @Test
  void handleValidationErrors_shouldReturn400() {

    MethodArgumentNotValidException exception =
        org.mockito.Mockito.mock(MethodArgumentNotValidException.class);

    org.springframework.validation.BindingResult bindingResult =
        org.mockito.Mockito.mock(org.springframework.validation.BindingResult.class);

    org.mockito.Mockito.when(exception.getBindingResult()).thenReturn(bindingResult);

    org.mockito.Mockito.when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());

    ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals(400, response.getBody().get("status"));

    assertEquals("VALIDATION_FAILED", response.getBody().get("error"));

    assertEquals("Request validation failed", response.getBody().get("message"));

    assertNotNull(response.getBody().get("errors"));
  }

  @Test
  void handleGeneralException_shouldReturn500() {

    Exception exception = new Exception("Something went wrong");

    ResponseEntity<Map<String, Object>> response = handler.handleGeneralException(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals(500, response.getBody().get("status"));

    assertEquals("INTERNAL_SERVER_ERROR", response.getBody().get("error"));

    assertEquals("An unexpected error occurred", response.getBody().get("message"));
  }
}
