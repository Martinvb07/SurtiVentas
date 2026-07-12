package com.surtiventas.backend.order.dto;

import com.surtiventas.backend.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long customerId,
        String customerName,
        OrderStatus status,
        Long createdById,
        Long assignedDriverId,
        String assignedDriverName,
        BigDecimal totalAmount,
        List<OrderLineResponse> lines,
        Instant createdAt,
        Instant updatedAt
) {
}
