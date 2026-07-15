package com.surtiventas.backend.purchasing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * The scanned supplier invoice attached to a purchase order and its
 * reconciliation against the order total. {@code fileData} is deliberately
 * excluded; the raw file is served from a separate download endpoint.
 */
public record SupplierInvoiceResponse(
        Long id,
        Long purchaseOrderId,
        String fileName,
        String contentType,
        String extractedText,
        BigDecimal detectedTotal,
        BigDecimal poTotal,
        BigDecimal difference,
        boolean matched,
        List<SupplierInvoiceLineResponse> lines,
        String uploadedByName,
        Instant createdAt
) {
}
