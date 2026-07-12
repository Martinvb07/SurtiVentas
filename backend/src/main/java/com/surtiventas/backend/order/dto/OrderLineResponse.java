package com.surtiventas.backend.order.dto;

import java.math.BigDecimal;

public record OrderLineResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
