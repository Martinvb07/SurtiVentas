package com.surtiventas.backend.order.dto;

import com.surtiventas.backend.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderTransitionRequest(
        @NotNull OrderStatus targetStatus,
        String note,
        Long driverId
) {
}
