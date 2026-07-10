package com.surtiventas.backend.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserSummary user
) {
}
