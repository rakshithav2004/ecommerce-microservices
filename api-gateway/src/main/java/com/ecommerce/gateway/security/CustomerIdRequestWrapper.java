package com.ecommerce.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class CustomerIdRequestWrapper extends HttpServletRequestWrapper {

  private final String customerId;

  public CustomerIdRequestWrapper(HttpServletRequest request, String customerId) {
    super(request);
    this.customerId = customerId;
  }

  @Override
  public String getHeader(String name) {

    if ("X-Customer-Id".equalsIgnoreCase(name)) {
      return customerId;
    }

    return super.getHeader(name);
  }

  @Override
  public java.util.Enumeration<String> getHeaders(String name) {

    if ("X-Customer-Id".equalsIgnoreCase(name)) {
      return java.util.Collections.enumeration(java.util.List.of(customerId));
    }

    return super.getHeaders(name);
  }

  @Override
  public java.util.Enumeration<String> getHeaderNames() {

    java.util.List<String> headerNames = java.util.Collections.list(super.getHeaderNames());

    if (!headerNames.stream().anyMatch(name -> "X-Customer-Id".equalsIgnoreCase(name))) {

      headerNames.add("X-Customer-Id");
    }

    return java.util.Collections.enumeration(headerNames);
  }
}
