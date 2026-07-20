package com.surtiventas.backend.replenishment.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Replenishment suggestions grouped by supplier, so each group can be turned
 * into one purchase order. {@code supplierId} is null for the "no supplier"
 * group (low-stock products nobody is registered to supply).
 */
public record SupplierReplenishment(
        Long supplierId,
        String supplierName,
        List<ReplenishmentItem> items,
        BigDecimal totalEstimatedCost
) {
}
