package com.surtiventas.backend.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderCreateRequest(
        @NotNull @DecimalMin(value = "0.0") BigDecimal totalAmount
) {
}
