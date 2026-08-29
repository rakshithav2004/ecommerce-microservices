package com.ecommerce.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class CustomerIdForwardingFilterTest {

  private CustomerIdForwardingFilter filter;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    filter = new CustomerIdForwardingFilter();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    filterChain = mock(FilterChain.class);

    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldForwardCustomerIdForOrderRequest() throws ServletException, IOException {

    Authentication authentication = mock(Authentication.class);

    when(authentication.getDetails()).thenReturn("customer-123");
    when(request.getRequestURI()).thenReturn("/api/v1/orders");

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filter.doFilter(request, response, filterChain);

    var wrappedRequestCaptor = org.mockito.ArgumentCaptor.forClass(CustomerIdRequestWrapper.class);

    verify(filterChain)
        .doFilter(wrappedRequestCaptor.capture(), org.mockito.ArgumentMatchers.eq(response));

    CustomerIdRequestWrapper wrappedRequest = wrappedRequestCaptor.getValue();

    assertEquals("customer-123", wrappedRequest.getHeader("X-Customer-Id"));
  }

  @Test
  void shouldPassOriginalRequestWhenUserIsNotAuthenticated() throws ServletException, IOException {

    when(request.getRequestURI()).thenReturn("/api/v1/orders");

    SecurityContextHolder.clearContext();

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldPassOriginalRequestForNonOrderEndpoint() throws ServletException, IOException {

    Authentication authentication = mock(Authentication.class);

    when(authentication.getDetails()).thenReturn("customer-123");
    when(request.getRequestURI()).thenReturn("/api/v1/products");

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldPassOriginalRequestWhenAuthenticationDetailsAreNotString()
      throws ServletException, IOException {

    Authentication authentication = mock(Authentication.class);

    Object authenticationDetails = new Object();

    when(authentication.getDetails()).thenReturn(authenticationDetails);
    when(request.getRequestURI()).thenReturn("/api/v1/orders");

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }
}
