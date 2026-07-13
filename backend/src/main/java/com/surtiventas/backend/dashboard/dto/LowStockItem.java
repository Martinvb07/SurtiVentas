package com.surtiventas.backend.dashboard.dto;

public record LowStockItem(Long id, String sku, String name, int stock, int minStock) {
}
