package com.surtiventas.backend.notification.dto;

import java.time.Instant;

/** A real-time notification pushed to the role responsible for the next step. */
public record NotificationMessage(
        String type,
        String title,
        String message,
        Long orderId,
        String orderNumber,
        Instant timestamp) {
}
