package com.surtiventas.backend.customer.dto;

import com.surtiventas.backend.customer.CustomerClassification;

import java.math.BigDecimal;
import java.time.Instant;

public record CustomerResponse(
        Long id,
        String storeName,
        String ownerName,
        String phone,
        String email,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal creditLimit,
        BigDecimal currentDebt,
        boolean overCreditLimit,
        CustomerClassification classification,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
