package com.surtiventas.backend.purchasing.dto;

import com.surtiventas.backend.purchasing.PurchaseOrderStatus;
import jakarta.validation.constraints.NotNull;

public record PurchaseOrderTransitionRequest(
        @NotNull PurchaseOrderStatus targetStatus,
        String note
) {
}
