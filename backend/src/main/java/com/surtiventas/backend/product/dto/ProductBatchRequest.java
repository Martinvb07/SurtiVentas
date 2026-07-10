package com.surtiventas.backend.product.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProductBatchRequest(
        @NotBlank String batchNumber,
        @NotNull @Min(0) Integer quantity,
        @NotNull @FutureOrPresent LocalDate expirationDate
) {
}
