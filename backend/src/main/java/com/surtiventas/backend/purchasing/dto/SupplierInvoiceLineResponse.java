package com.surtiventas.backend.purchasing.dto;

import java.math.BigDecimal;

/** A candidate line item the OCR parser detected on the supplier invoice. */
public record SupplierInvoiceLineResponse(
        String description,
        Integer quantity,
        BigDecimal amount
) {
}
