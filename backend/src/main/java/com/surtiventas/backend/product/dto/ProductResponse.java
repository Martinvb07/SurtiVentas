package com.surtiventas.backend.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        CategoryResponse category,
        UnitOfMeasureResponse unitOfMeasure,
        BigDecimal price,
        Integer stock,
        Integer minStock,
        boolean lowStock,
        boolean batchTracked,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
