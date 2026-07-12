package com.surtiventas.backend.customer.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DebtMovementResponse(
        Long id,
        BigDecimal amountDelta,
        String reason,
        Long createdById,
        String createdByName,
        Instant createdAt
) {
}
