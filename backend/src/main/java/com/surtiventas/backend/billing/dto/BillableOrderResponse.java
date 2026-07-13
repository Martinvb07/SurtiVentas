package com.surtiventas.backend.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** A delivered order that is awaiting invoicing. */
public record BillableOrderResponse(
        Long orderId,
        String orderNumber,
        Long customerId,
        String customerName,
        BigDecimal totalAmount,
        Instant createdAt) {
}
