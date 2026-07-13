package com.surtiventas.backend.geo.dto;

import java.math.BigDecimal;

/** A delivery stop plotted on the driver's route map. */
public record DeliveryPoint(
        Long orderId,
        String orderNumber,
        String customerName,
        String address,
        double latitude,
        double longitude,
        BigDecimal totalAmount) {
}
