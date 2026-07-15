package com.surtiventas.backend.finance.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Income report: what was invoiced (sales), collected (payments) and spent on
 * merchandise (committed purchases), this month and all-time, plus a monthly
 * trend. Profit = collected - purchases.
 */
public record IncomeReportResponse(
        BigDecimal invoicedMonth,
        BigDecimal collectedMonth,
        BigDecimal purchasesMonth,
        BigDecimal profitMonth,
        BigDecimal invoicedTotal,
        BigDecimal collectedTotal,
        BigDecimal purchasesTotal,
        BigDecimal profitTotal,
        List<MonthlyPoint> trend) {
}
