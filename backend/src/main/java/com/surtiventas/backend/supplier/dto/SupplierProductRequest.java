package com.surtiventas.backend.supplier.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SupplierProductRequest(
        @NotNull Long productId,
        @Size(max = 40) String supplierSku,
        @NotNull @DecimalMin(value = "0.0") BigDecimal cost
) {
}
