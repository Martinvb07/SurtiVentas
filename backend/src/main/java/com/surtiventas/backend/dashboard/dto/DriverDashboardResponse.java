package com.surtiventas.backend.dashboard.dto;

import java.util.List;

/** Route/delivery overview for the CONDUCTOR. */
public record DriverDashboardResponse(
        long assignedToMe,
        long deliveredToday,
        long incidentsOpen,
        long deliveredMonth,
        List<StatusCount> deliveryBreakdown,
        List<RecentOrder> myDeliveries) {
}
