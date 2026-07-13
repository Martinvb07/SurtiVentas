package com.surtiventas.backend.billing.dto;

import com.surtiventas.backend.billing.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        Long orderId,
        String orderNumber,
        Long customerId,
        String customerName,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balance,
        InvoiceStatus status,
        boolean overdue,
        Instant issuedAt,
        List<PaymentResponse> payments) {
}
