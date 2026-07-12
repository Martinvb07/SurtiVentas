package com.surtiventas.backend.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DebtMovementRequest(
        @NotNull BigDecimal amountDelta,
        @NotBlank String reason
) {
}
