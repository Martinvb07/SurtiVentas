package com.surtiventas.backend.purchasing.dto;

import com.surtiventas.backend.purchasing.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String orderNumber,
        Long supplierId,
        String supplierName,
        PurchaseOrderStatus status,
        LocalDate expectedDate,
        BigDecimal totalAmount,
        Long createdById,
        List<PurchaseOrderLineResponse> lines,
        Instant createdAt,
        Instant updatedAt
) {
}
