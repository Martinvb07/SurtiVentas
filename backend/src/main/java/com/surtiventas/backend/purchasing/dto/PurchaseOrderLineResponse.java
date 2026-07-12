package com.surtiventas.backend.purchasing.dto;

import java.math.BigDecimal;

public record PurchaseOrderLineResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        Integer quantity,
        BigDecimal unitCost,
        BigDecimal subtotal
) {
}
