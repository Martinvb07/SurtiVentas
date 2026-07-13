package com.surtiventas.backend.dashboard.dto;

import java.util.List;

/** Work-queue overview for the BODEGUERO / alistador. */
public record WarehouseDashboardResponse(
        long toPick,
        long inProgress,
        long readyForDispatch,
        long lowStockCount,
        long purchaseOrdersToReceive,
        List<StatusCount> pickingFunnel,
        List<LowStockItem> lowStockItems) {
}
