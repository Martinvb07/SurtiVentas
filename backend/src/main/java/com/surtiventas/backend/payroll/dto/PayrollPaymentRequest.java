package com.surtiventas.backend.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayrollPaymentRequest(
        @NotNull @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero") BigDecimal amount,
        @NotBlank String period,
        String note) {
}
