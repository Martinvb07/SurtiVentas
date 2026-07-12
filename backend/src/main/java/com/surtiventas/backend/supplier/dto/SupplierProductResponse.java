package com.surtiventas.backend.supplier.dto;

import java.math.BigDecimal;

public record SupplierProductResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        String supplierSku,
        BigDecimal cost
) {
}
