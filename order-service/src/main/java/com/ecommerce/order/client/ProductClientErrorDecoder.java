package com.ecommerce.order.client;

import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.ProductServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class ProductClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(
            String methodKey,
            Response response) {

        if (response.status() == 404) {
            return new ProductServiceException(
                    "Product not found",
                    404
            );
        }

        if (response.status() == 400) {

            if (methodKey.contains("reserveStock")) {
                return new InsufficientStockException(
                        "Insufficient stock for product"
                );
            }

            return new ProductServiceException(
                    "Invalid product request",
                    400
            );
        }

        if (response.status() >= 500) {
            return new ProductServiceException(
                    "Product Service is currently unavailable",
                    503
            );
        }

        return new ProductServiceException(
                "Product Service request failed with status: "
                        + response.status(),
                response.status()
        );
    }
}