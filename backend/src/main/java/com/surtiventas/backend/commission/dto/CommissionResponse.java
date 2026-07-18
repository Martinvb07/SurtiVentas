package com.surtiventas.backend.commission.dto;

import java.math.BigDecimal;

/**
 * A salesperson's commission for a month: their achieved sales against the
 * target (if any), whether the goal was met, the rate applied, and the
 * resulting commission. Goal-related fields are null when no goal is set.
 */
public record CommissionResponse(
        Long sellerId,
        String sellerName,
        String month,
        BigDecimal targetAmount,
        BigDecimal commissionRate,
        BigDecimal bonusRate,
        BigDecimal achievedSales,
        BigDecimal attainmentPct,
        boolean hasGoal,
        boolean goalMet,
        BigDecimal appliedRate,
        BigDecimal commission
) {
}
