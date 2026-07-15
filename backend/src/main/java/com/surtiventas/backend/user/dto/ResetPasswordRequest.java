package com.surtiventas.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password) {
}
