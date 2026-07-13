package com.surtiventas.backend.billing.dto;

import com.surtiventas.backend.billing.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        BigDecimal amount,
        PaymentMethod method,
        String reference,
        String registeredByName,
        Instant paidAt) {
}
