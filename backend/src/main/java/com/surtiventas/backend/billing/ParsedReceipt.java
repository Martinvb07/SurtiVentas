package com.surtiventas.backend.billing;

import java.math.BigDecimal;

/**
 * Best-effort structured view of an OCR'd payment receipt: the detected amount
 * (null when none could be read) and the reference/transaction number, if any.
 */
public record ParsedReceipt(BigDecimal detectedAmount, String detectedReference) {
}
