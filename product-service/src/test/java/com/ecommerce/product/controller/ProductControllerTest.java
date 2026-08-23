package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void createProduct_shouldReturn201() throws Exception {

        ProductRequest request = new ProductRequest(
                "PHONE-001",
                "Samsung Galaxy",
                "Electronics",
                "Samsung smartphone",
                new BigDecimal("25000"),
                10
        );

        ProductResponse response = new ProductResponse(
                "product-001",
                "PHONE-001",
                "Samsung Galaxy",
                "Electronics",
                "Samsung smartphone",
                new BigDecimal("25000"),
                10,
                true
        );

        when(productService.createProduct(any(ProductRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("product-001"))
                .andExpect(jsonPath("$.sku").value("PHONE-001"))
                .andExpect(jsonPath("$.name").value("Samsung Galaxy"))
                .andExpect(jsonPath("$.stock").value(10));

        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    void getProductById_shouldReturn200() throws Exception {

        ProductResponse response = new ProductResponse(
                "product-001",
                "PHONE-001",
                "Samsung Galaxy",
                "Electronics",
                "Samsung smartphone",
                new BigDecimal("25000"),
                10,
                true
        );

        when(productService.getProductById("product-001"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/products/product-001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product-001"))
                .andExpect(jsonPath("$.sku").value("PHONE-001"))
                .andExpect(jsonPath("$.name").value("Samsung Galaxy"));

        verify(productService).getProductById("product-001");
    }

    @Test
    void getProductById_shouldReturn404WhenProductNotFound()
            throws Exception {

        when(productService.getProductById("invalid-id"))
                .thenThrow(
                        new ProductNotFoundException(
                                "Product not found with id: invalid-id"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/products/invalid-id")
                )
                .andExpect(status().isNotFound());

        verify(productService).getProductById("invalid-id");
    }

    @Test
    void getAllProducts_shouldReturn200() throws Exception {

        ProductResponse response = new ProductResponse(
                "product-001",
                "PHONE-001",
                "Samsung Galaxy",
                "Electronics",
                "Samsung smartphone",
                new BigDecimal("25000"),
                10,
                true
        );

        PageImpl<ProductResponse> page =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                );

        when(productService.getAllProducts(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/products")
                                .param("category", "Electronics")
                                .param("search", "Samsung")
                                .param("minPrice", "10000")
                                .param("maxPrice", "50000")
                                .param("inStock", "true")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value("product-001"))
                .andExpect(jsonPath("$.content[0].sku")
                        .value("PHONE-001"));

        verify(productService).getAllProducts(
                eq("Electronics"),
                eq("Samsung"),
                eq(new BigDecimal("10000")),
                eq(new BigDecimal("50000")),
                eq(true),
                any()
        );
    }

    @Test
    void updateProduct_shouldReturn200() throws Exception {

        ProductRequest request = new ProductRequest(
                "PHONE-001",
                "Samsung Galaxy Updated",
                "Electronics",
                "Updated smartphone",
                new BigDecimal("27000"),
                15
        );

        ProductResponse response = new ProductResponse(
                "product-001",
                "PHONE-001",
                "Samsung Galaxy Updated",
                "Electronics",
                "Updated smartphone",
                new BigDecimal("27000"),
                15,
                true
        );

        when(productService.updateProduct(
                eq("product-001"),
                any(ProductRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/products/product-001")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Samsung Galaxy Updated"))
                .andExpect(jsonPath("$.price")
                        .value(27000))
                .andExpect(jsonPath("$.stock")
                        .value(15));

        verify(productService).updateProduct(
                eq("product-001"),
                any(ProductRequest.class)
        );
    }

    @Test
    void deleteProduct_shouldReturn204() throws Exception {

        doNothing()
                .when(productService)
                .deleteProduct("product-001");

        mockMvc.perform(
                        delete("/api/v1/products/product-001")
                )
                .andExpect(status().isNoContent());

        verify(productService)
                .deleteProduct("product-001");
    }

    @Test
    void reserveStock_shouldReturn200() throws Exception {

        ProductResponse response = new ProductResponse(
                "product-001",
                "PHONE-001",
                "Samsung Galaxy",
                "Electronics",
                "Samsung smartphone",
                new BigDecimal("25000"),
                8,
                true
        );

        when(productService.reserveStock(
                "product-001",
                2
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/products/product-001/reserve-stock")
                                .param("quantity", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(8));

        verify(productService)
                .reserveStock("product-001", 2);
    }

    @Test
    void releaseStock_shouldReturn200() throws Exception {

        ProductResponse response = new ProductResponse(
                "product-001",
                "PHONE-001",
                "Samsung Galaxy",
                "Electronics",
                "Samsung smartphone",
                new BigDecimal("25000"),
                12,
                true
        );

        when(productService.releaseStock(
                "product-001",
                2
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/products/product-001/release-stock")
                                .param("quantity", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(12));

        verify(productService)
                .releaseStock("product-001", 2);
    }

    @Test
    void createProduct_shouldReturn400ForInvalidRequest()
            throws Exception {
        ProductRequest invalidRequest = new ProductRequest(
                "",
                "",
                "",
                "",
                new BigDecimal("-100"),
                -1
        );

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verify(productService, never())
                .createProduct(any(ProductRequest.class));
    }
}