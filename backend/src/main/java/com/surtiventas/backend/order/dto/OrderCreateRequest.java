package com.surtiventas.backend.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateRequest(
        @NotNull Long customerId,
        @NotEmpty @Valid List<OrderLineRequest> lines,
        /**
         * Optional client-generated idempotency key (UUID) for offline-synced
         * orders; when a matching order already exists it is returned unchanged
         * instead of creating a duplicate.
         */
        @Size(max = 36) String clientRequestId
) {
}
