package com.surtiventas.backend.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The scanned payment receipt attached to a payment and its reconciliation
 * against the registered amount. {@code fileData} is excluded; the raw file is
 * served from a separate download endpoint.
 */
public record PaymentReceiptResponse(
        Long id,
        Long paymentId,
        String fileName,
        String contentType,
        String extractedText,
        BigDecimal detectedAmount,
        String detectedReference,
        BigDecimal paymentAmount,
        BigDecimal difference,
        boolean matched,
        String uploadedByName,
        Instant createdAt
) {
}
