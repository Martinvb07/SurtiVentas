package com.surtiventas.backend.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RecentOrder(
        Long id,
        String orderNumber,
        String customerName,
        String status,
        BigDecimal totalAmount,
        Instant createdAt) {
}
