package com.surtiventas.backend.purchasing;

import java.math.BigDecimal;
import java.util.List;

/**
 * Best-effort structured view of an OCR'd supplier invoice: the grand total the
 * parser detected (null when none could be read) and the candidate line items.
 */
public record ParsedInvoice(BigDecimal detectedTotal, List<ParsedLine> lines) {

    public record ParsedLine(String description, Integer quantity, BigDecimal amount) {
    }
}
