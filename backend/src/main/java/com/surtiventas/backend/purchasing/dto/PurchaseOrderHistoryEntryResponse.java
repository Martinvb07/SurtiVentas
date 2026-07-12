package com.surtiventas.backend.purchasing.dto;

import com.surtiventas.backend.purchasing.PurchaseOrderStatus;

import java.time.Instant;

public record PurchaseOrderHistoryEntryResponse(
        Long id,
        PurchaseOrderStatus fromStatus,
        PurchaseOrderStatus toStatus,
        Long changedById,
        String changedByName,
        String note,
        Instant changedAt
) {
}
