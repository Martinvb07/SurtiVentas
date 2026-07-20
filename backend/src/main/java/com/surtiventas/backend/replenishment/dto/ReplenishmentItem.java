package com.surtiventas.backend.replenishment.dto;

import java.math.BigDecimal;

/**
 * A single suggested replenishment line: a low-stock product, its recent demand,
 * the suggested reorder quantity, and the (cheapest) supplier's cost. Cost
 * fields are null when no supplier offers the product.
 */
public record ReplenishmentItem(
        Long productId,
        String sku,
        String name,
        int stock,
        int minStock,
        int demand30,
        int suggestedQty,
        BigDecimal unitCost,
        BigDecimal estimatedCost
) {
}
