package com.surtiventas.backend.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/** Collection/billing overview for the FACTURADOR. */
public record BillingDashboardResponse(
        long ordersToBill,
        BigDecimal totalReceivables,
        long customersOverLimit,
        long paidThisMonth,
        List<SeriesPoint> receivablesByClassification,
        List<Debtor> topDebtors,
        List<RecentOrder> ordersToBillQueue) {
}
