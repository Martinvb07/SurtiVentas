package com.surtiventas.backend.dashboard.dto;

import java.math.BigDecimal;

public record Debtor(Long id, String storeName, BigDecimal currentDebt, BigDecimal creditLimit, boolean overLimit) {
}
