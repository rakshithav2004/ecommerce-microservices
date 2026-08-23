package com.ecommerce.order.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleOrderNotFoundException() {

        OrderNotFoundException exception =
                new OrderNotFoundException("Order not found: ORD-001");

        ResponseEntity<Map<String, Object>> response =
                handler.handleOrderNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        assertNotNull(response.getBody());

        assertEquals(404, response.getBody().get("status"));
        assertEquals("ORDER_NOT_FOUND",
                response.getBody().get("error"));
        assertEquals("Order not found: ORD-001",
                response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void shouldHandleIllegalStateException() {

        IllegalStateException exception =
                new IllegalStateException("Product Service unavailable");

        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalStateException(exception);

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(503, response.getBody().get("status"));
        assertEquals("Service Unavailable",
                response.getBody().get("error"));
        assertEquals("Product Service unavailable",
                response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void shouldHandleGeneralException() {

        Exception exception =
                new Exception("Unexpected error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneralException(exception);

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(503, response.getBody().get("status"));
        assertEquals("SERVICE_UNAVAILABLE",
                response.getBody().get("error"));

        assertEquals(
                "Product Service is currently unavailable. Please try again later.",
                response.getBody().get("message")
        );

        assertNotNull(response.getBody().get("timestamp"));
    }
}