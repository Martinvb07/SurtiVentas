package com.surtiventas.backend.payroll.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PayrollPaymentResponse(
        Long id,
        Long employeeId,
        String employeeName,
        BigDecimal amount,
        String period,
        String note,
        String registeredByName,
        Instant paidAt) {
}
