package com.ecommerce.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerIdRequestWrapperTest {

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);

        when(request.getHeaderNames())
                .thenReturn(Collections.enumeration(java.util.List.of("Authorization", "Content-Type")));
    }

    @Test
    void shouldReturnCustomerIdFromHeader() {

        CustomerIdRequestWrapper wrapper = new CustomerIdRequestWrapper(request, "customer-123");

        assertEquals("customer-123", wrapper.getHeader("X-Customer-Id"));
    }

    @Test
    void shouldReturnCustomerIdRegardlessOfHeaderCase() {

        CustomerIdRequestWrapper wrapper = new CustomerIdRequestWrapper(request, "customer-123");

        assertEquals("customer-123", wrapper.getHeader("x-customer-id"));

        assertEquals("customer-123", wrapper.getHeader("X-CUSTOMER-ID"));
    }

    @Test
    void shouldReturnCustomerIdFromGetHeaders() {

        CustomerIdRequestWrapper wrapper = new CustomerIdRequestWrapper(request, "customer-123");

        Enumeration<String> headers = wrapper.getHeaders("X-Customer-Id");

        assertTrue(headers.hasMoreElements());
        assertEquals("customer-123", headers.nextElement());
    }

    @Test
    void shouldIncludeCustomerIdInHeaderNames() {

        CustomerIdRequestWrapper wrapper = new CustomerIdRequestWrapper(request, "customer-123");

        Enumeration<String> headerNames = wrapper.getHeaderNames();

        java.util.List<String> names = Collections.list(headerNames);

        assertTrue(names.stream().anyMatch(name -> "X-Customer-Id".equalsIgnoreCase(name)));
    }

    @Test
    void shouldDelegateUnknownHeaderToOriginalRequest() {

        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        CustomerIdRequestWrapper wrapper = new CustomerIdRequestWrapper(request, "customer-123");

        assertEquals("Bearer test-token", wrapper.getHeader("Authorization"));
    }
}
