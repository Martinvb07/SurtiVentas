package com.surtiventas.backend.user.dto;

import com.surtiventas.backend.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpdateRequest(
        @NotBlank String fullName,
        @NotNull Role role,
        boolean active) {
}
