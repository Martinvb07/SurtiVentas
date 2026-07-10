package com.surtiventas.backend.order.dto;

import com.surtiventas.backend.order.OrderStatus;

import java.time.Instant;

public record OrderHistoryEntryResponse(
        Long id,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        Long changedById,
        String changedByName,
        String note,
        Instant changedAt
) {
}
