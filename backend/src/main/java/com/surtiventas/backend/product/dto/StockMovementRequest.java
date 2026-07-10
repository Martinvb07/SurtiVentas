package com.surtiventas.backend.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockMovementRequest(
        @NotNull Integer quantityDelta,
        @NotBlank String reason
) {
}
