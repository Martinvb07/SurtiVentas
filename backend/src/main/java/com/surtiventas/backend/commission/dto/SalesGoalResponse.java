package com.surtiventas.backend.commission.dto;

import java.math.BigDecimal;

public record SalesGoalResponse(
        Long id,
        Long sellerId,
        String sellerName,
        String month,
        BigDecimal targetAmount,
        BigDecimal commissionRate,
        BigDecimal bonusRate
) {
}
