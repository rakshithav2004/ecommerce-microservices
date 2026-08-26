package com.ecommerce.order.dto;

import com.ecommerce.order.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record PaymentUpdateRequest(

        @NotNull(message = "Payment status is required")
        PaymentStatus paymentStatus

) {
}