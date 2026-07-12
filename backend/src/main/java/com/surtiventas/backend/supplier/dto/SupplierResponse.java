package com.surtiventas.backend.supplier.dto;

import java.time.Instant;

public record SupplierResponse(
        Long id,
        String name,
        String contactName,
        String phone,
        String email,
        String address,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
