package com.surtiventas.backend.billing.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GenerateInvoiceRequest(
        @NotNull Long orderId,
        @NotNull LocalDate dueDate) {
}
