package com.surtiventas.backend.finance.dto;

import java.math.BigDecimal;

/** Invoiced, collected, purchases and payroll for one month of the income trend. */
public record MonthlyPoint(
        String label,
        BigDecimal invoiced,
        BigDecimal collected,
        BigDecimal purchases,
        BigDecimal payroll) {
}
