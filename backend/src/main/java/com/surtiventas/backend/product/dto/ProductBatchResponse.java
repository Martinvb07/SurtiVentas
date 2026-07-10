package com.surtiventas.backend.product.dto;

import java.time.LocalDate;

public record ProductBatchResponse(
        Long id,
        String batchNumber,
        Integer quantity,
        LocalDate expirationDate
) {
}
