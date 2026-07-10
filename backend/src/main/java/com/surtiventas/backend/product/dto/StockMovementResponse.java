package com.surtiventas.backend.product.dto;

import java.time.Instant;

public record StockMovementResponse(
        Long id,
        Integer quantityDelta,
        String reason,
        Long createdById,
        String createdByName,
        Instant createdAt
) {
}
