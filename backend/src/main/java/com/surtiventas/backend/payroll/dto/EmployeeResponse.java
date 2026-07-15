package com.surtiventas.backend.payroll.dto;

import java.math.BigDecimal;

public record EmployeeResponse(
        Long id,
        String fullName,
        String position,
        BigDecimal salary,
        boolean active) {
}
