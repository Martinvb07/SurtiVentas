package com.surtiventas.backend.portal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Account statement shown on the buyer's self-service portal home. */
public record PortalSummaryResponse(
        Long customerId,
        String storeName,
        String ownerName,
        BigDecimal currentDebt,
        BigDecimal creditLimit,
        BigDecimal availableCredit,
        boolean overLimit,
        long totalOrders,
        long pendingInvoices,
        long overdueInvoices,
        LocalDate nextDueDate) {
}
