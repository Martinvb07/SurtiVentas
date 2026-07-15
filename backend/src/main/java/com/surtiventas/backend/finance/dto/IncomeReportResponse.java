package com.surtiventas.backend.finance.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Income report: what was invoiced (sales), collected (payments), spent on
 * merchandise (committed purchases) and paid in payroll, this month and
 * all-time, plus a monthly trend. Profit = collected - purchases - payroll.
 */
public record IncomeReportResponse(
        BigDecimal invoicedMonth,
        BigDecimal collectedMonth,
        BigDecimal purchasesMonth,
        BigDecimal payrollMonth,
        BigDecimal profitMonth,
        BigDecimal invoicedTotal,
        BigDecimal collectedTotal,
        BigDecimal purchasesTotal,
        BigDecimal payrollTotal,
        BigDecimal profitTotal,
        List<MonthlyPoint> trend) {
}
