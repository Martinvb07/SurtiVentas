package com.surtiventas.backend.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/** "Mi trabajo" overview for the VENDEDOR. */
public record SellerDashboardResponse(
        BigDecimal mySalesMonth,
        long myOrdersMonth,
        long myOrdersInProgress,
        long customersServedMonth,
        List<SeriesPoint> mySalesTrend,
        List<StatusCount> myOrdersByStatus,
        List<RecentOrder> recentOrders) {
}
