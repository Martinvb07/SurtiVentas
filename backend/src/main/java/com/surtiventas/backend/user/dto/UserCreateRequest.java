package com.surtiventas.backend.user.dto;

import com.surtiventas.backend.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Email String email,
        @NotBlank String fullName,
        @NotNull Role role,
        @NotBlank @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password) {
}
