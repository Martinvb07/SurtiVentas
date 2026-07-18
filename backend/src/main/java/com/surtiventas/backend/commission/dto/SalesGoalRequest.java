package com.surtiventas.backend.commission.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Sets (or updates) a salesperson's goal for a month. {@code month} is
 * "YYYY-MM"; rates are percentages (2.5 = 2.5%).
 */
public record SalesGoalRequest(
        @NotNull Long sellerId,
        @NotNull @Pattern(regexp = "\\d{4}-\\d{2}", message = "El mes debe tener formato YYYY-MM") String month,
        @NotNull @PositiveOrZero BigDecimal targetAmount,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal commissionRate,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal bonusRate
) {
}
