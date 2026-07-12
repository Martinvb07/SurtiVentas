package com.surtiventas.backend.purchasing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderCreateRequest(
        @NotNull Long supplierId,
        LocalDate expectedDate,
        @NotEmpty @Valid List<PurchaseOrderLineRequest> lines
) {
}
