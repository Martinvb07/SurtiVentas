package com.surtiventas.backend.billing.dto;

/** A line of the order the biller must digitalize, with its current stock. */
public record InvoiceLineReview(
        Long productId,
        String productName,
        String sku,
        int quantity,
        int stock,
        boolean sufficient) {
}
