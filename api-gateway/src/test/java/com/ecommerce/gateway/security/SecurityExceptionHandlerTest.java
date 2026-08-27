package com.ecommerce.gateway.security;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class SecurityExceptionHandlerTest {

  private SecurityExceptionHandler handler;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {

    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.registerModule(new JavaTimeModule());

    handler = new SecurityExceptionHandler(objectMapper);

    request = new MockHttpServletRequest();

    response = new MockHttpServletResponse();
  }

  @Test
  void commence_shouldReturn401Unauthorized() throws IOException {

    BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

    handler.commence(request, response, exception);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

    assertEquals("application/json", response.getContentType());

    String responseBody = response.getContentAsString();

    assertTrue(responseBody.contains("\"status\":401"));

    assertTrue(responseBody.contains("\"error\":\"UNAUTHORIZED\""));

    assertTrue(responseBody.contains("\"message\":\"Authentication is required\""));

    assertTrue(responseBody.contains("\"timestamp\""));
  }

  @Test
  void handle_shouldReturn403Forbidden() throws IOException {

    AccessDeniedException exception = new AccessDeniedException("Access denied");

    handler.handle(request, response, exception);

    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());

    assertEquals("application/json", response.getContentType());

    String responseBody = response.getContentAsString();

    assertTrue(responseBody.contains("\"status\":403"));

    assertTrue(responseBody.contains("\"error\":\"FORBIDDEN\""));

    assertTrue(
        responseBody.contains(
            "\"message\":\"You do not have permission to access this resource\""));

    assertTrue(responseBody.contains("\"timestamp\""));
  }
}
