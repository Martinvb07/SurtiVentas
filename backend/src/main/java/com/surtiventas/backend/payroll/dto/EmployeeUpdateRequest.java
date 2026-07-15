package com.surtiventas.backend.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EmployeeUpdateRequest(
        @NotBlank String fullName,
        @NotBlank String position,
        @NotNull @DecimalMin("0") BigDecimal salary,
        boolean active) {
}
