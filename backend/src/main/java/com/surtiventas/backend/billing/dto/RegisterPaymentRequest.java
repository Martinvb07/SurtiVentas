package com.surtiventas.backend.billing.dto;

import com.surtiventas.backend.billing.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RegisterPaymentRequest(
        @NotNull @DecimalMin(value = "0.01", message = "El abono debe ser mayor que cero") BigDecimal amount,
        @NotNull PaymentMethod method,
        String reference) {
}
