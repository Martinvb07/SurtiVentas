package com.surtiventas.backend.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/** Global business overview for the ADMINISTRADOR / gerente. */
public record AdminDashboardResponse(
        BigDecimal salesToday,
        BigDecimal salesMonth,
        long ordersInProgress,
        BigDecimal totalReceivables,
        long lowStockCount,
        long activeCustomers,
        List<SeriesPoint> salesTrend,
        List<StatusCount> ordersByStatus,
        List<TopProduct> topProducts,
        List<SeriesPoint> salesBySeller) {
}
