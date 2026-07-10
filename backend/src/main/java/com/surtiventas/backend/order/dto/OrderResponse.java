package com.surtiventas.backend.order.dto;

import com.surtiventas.backend.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        Long createdById,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt
) {
}
