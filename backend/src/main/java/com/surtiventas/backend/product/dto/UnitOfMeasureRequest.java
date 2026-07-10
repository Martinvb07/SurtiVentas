package com.surtiventas.backend.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnitOfMeasureRequest(
        @NotBlank @Size(max = 60) String name,
        @NotBlank @Size(max = 10) String abbreviation
) {
}
